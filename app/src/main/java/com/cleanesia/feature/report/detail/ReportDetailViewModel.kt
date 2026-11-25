package com.cleanesia.feature.report.detail

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cleanesia.CleanesiaApp
import com.cleanesia.core.location.currentLocationOrNull
import com.cleanesia.core.location.hasLocationPermission
import com.cleanesia.core.photo.decodePhoto
import com.cleanesia.data.repository.ReportRepository
import com.cleanesia.feature.report.ReportExtras
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Keadaan kartu Lokasi. */
sealed interface LocationUi {
    data object Searching : LocationUi
    data class Found(val lat: Double, val lng: Double) : LocationUi
    data class Failed(val reason: Reason) : LocationUi {
        enum class Reason {
            /** Izin belum diberikan; "Coba lagi" meminta izin. */
            NO_PERMISSION,

            /** Izin ditolak permanen; hanya bisa diubah lewat pengaturan aplikasi. */
            PERMISSION_BLOCKED,

            /** Izin ada tapi posisi tidak didapat (lokasi perangkat mati atau sinyal belum ada). */
            UNAVAILABLE,
        }
    }
}

data class ReportDetailUiState(
    val photo: ImageBitmap? = null,
    val types: Set<WasteType> = emptySet(),
    val severity: Severity? = null,
    val location: LocationUi = LocationUi.Searching,
    val sending: Boolean = false,
    val sent: Boolean = false,
) {
    val canSend: Boolean
        get() = location is LocationUi.Found && severity != null && types.isNotEmpty() && !sending && !sent
}

class ReportDetailViewModel(
    private val app: Application,
    private val repository: ReportRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    /** Dari extra Intent ReportPhotoActivity (extra otomatis masuk ke SavedStateHandle). */
    private val photoPath: String = checkNotNull(savedState[ReportExtras.PHOTO_PATH]) {
        "ReportDetailActivity butuh extra PHOTO_PATH"
    }

    // Pilihan pengguna disimpan di SavedStateHandle supaya bertahan saat rotasi dan proses dimatikan.
    private val typeIndices = savedState.getStateFlow(KEY_TYPES, IntArray(0))
    private val severityLevel = savedState.getStateFlow(KEY_SEVERITY, 0)
    private val savedLocation = savedState.getStateFlow<DoubleArray?>(KEY_LOCATION, null)
    private val sent = savedState.getStateFlow(KEY_SENT, false)

    private val photo = MutableStateFlow<ImageBitmap?>(null)
    private val locationStatus = MutableStateFlow<LocationUi>(LocationUi.Searching)
    private val sending = MutableStateFlow(false)

    val uiState: StateFlow<ReportDetailUiState> = combine(
        combine(photo, typeIndices, severityLevel) { p, t, s -> Triple(p, t, s) },
        combine(savedLocation, locationStatus) { saved, status -> saved?.let { LocationUi.Found(it[0], it[1]) } ?: status },
        sending,
        sent,
    ) { (p, t, s), location, isSending, isSent ->
        ReportDetailUiState(
            photo = p,
            types = t.map { WasteType.fromIndex(it) }.toSet(),
            severity = if (s == 0) null else Severity.fromLevel(s),
            location = location,
            sending = isSending,
            sent = isSent,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportDetailUiState())

    init {
        viewModelScope.launch {
            photo.value = withContext(Dispatchers.IO) { decodePhoto(photoPath, PHOTO_MAX_SIDE_PX) }?.asImageBitmap()
        }
        if (savedLocation.value == null) fetchLocation()
    }

    fun toggleType(type: WasteType) {
        val current = typeIndices.value.toSet()
        val next = if (type.index in current) current - type.index else current + type.index
        savedState[KEY_TYPES] = next.sorted().toIntArray()
    }

    fun selectSeverity(severity: Severity) {
        savedState[KEY_SEVERITY] = severity.level
    }

    /** Ambil posisi sekali. Dipanggil saat layar dibuka dan dari tombol "Coba lagi". */
    fun fetchLocation() {
        if (savedLocation.value != null) return
        locationStatus.value = LocationUi.Searching
        viewModelScope.launch {
            val location = currentLocationOrNull(app)
            if (location != null) {
                savedState[KEY_LOCATION] = doubleArrayOf(location.latitude, location.longitude)
            } else {
                locationStatus.value = LocationUi.Failed(
                    if (hasLocationPermission(app)) LocationUi.Failed.Reason.UNAVAILABLE
                    else LocationUi.Failed.Reason.NO_PERMISSION
                )
            }
        }
    }

    /** Hasil permintaan izin dari layar Detail. */
    fun onPermissionResult(granted: Boolean, permanentlyDenied: Boolean) {
        if (granted) {
            fetchLocation()
        } else {
            locationStatus.value = LocationUi.Failed(
                if (permanentlyDenied) LocationUi.Failed.Reason.PERMISSION_BLOCKED
                else LocationUi.Failed.Reason.NO_PERMISSION
            )
        }
    }

    /** Dipanggil saat layar kembali aktif, misalnya setelah pengguna membuka pengaturan izin. */
    fun onResume() {
        val status = locationStatus.value
        if (savedLocation.value == null && status is LocationUi.Failed && hasLocationPermission(app)) {
            fetchLocation()
        }
    }

    fun send() {
        val state = uiState.value
        val location = state.location as? LocationUi.Found ?: return
        val severity = state.severity ?: return
        if (!state.canSend) return
        sending.value = true
        viewModelScope.launch {
            try {
                repository.addReport(
                    types = state.types.sortedBy { it.index },
                    severity = severity,
                    lat = location.lat,
                    lng = location.lng,
                    createdAt = System.currentTimeMillis(),
                    photoPath = photoPath,
                )
                savedState[KEY_SENT] = true
            } finally {
                sending.value = false
            }
        }
    }

    /**
     * ViewModel dibersihkan hanya saat Activity benar-benar selesai, bukan saat rotasi.
     * Kalau laporan belum terkirim, foto tidak dipakai lagi, jadi dihapus.
     */
    override fun onCleared() {
        if (!sent.value) File(photoPath).delete()
    }

    companion object {
        private const val KEY_TYPES = "types"
        private const val KEY_SEVERITY = "severity"
        private const val KEY_LOCATION = "location"
        private const val KEY_SENT = "sent"
        private const val PHOTO_MAX_SIDE_PX = 1600

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CleanesiaApp
                ReportDetailViewModel(app, app.reportRepository, createSavedStateHandle())
            }
        }
    }
}
