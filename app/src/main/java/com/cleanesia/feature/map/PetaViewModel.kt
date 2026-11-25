package com.cleanesia.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cleanesia.CleanesiaApp
import com.cleanesia.data.local.CellEntity
import com.cleanesia.data.repository.ReportRepository
import com.cleanesia.ui.components.MapCamera
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PetaViewModel(
    repository: ReportRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    val cells: StateFlow<List<CellEntity>> = repository.cells
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Posisi kamera terakhir. ViewModel bertahan selama MainActivity hidup, jadi nilai ini
     * tetap ada saat pindah tab dan rotasi. SavedStateHandle membuatnya bertahan juga
     * saat proses aplikasi dimatikan sistem di latar belakang.
     */
    var camera: MapCamera?
        get() = savedState.get<DoubleArray>(KEY_CAMERA)?.let { MapCamera(it[0], it[1], it[2]) }
        set(value) {
            savedState[KEY_CAMERA] = value?.let { doubleArrayOf(it.lat, it.lng, it.zoom) }
        }

    companion object {
        private const val KEY_CAMERA = "camera"

        val Factory = viewModelFactory {
            initializer {
                PetaViewModel(
                    repository = (this[APPLICATION_KEY] as CleanesiaApp).reportRepository,
                    savedState = createSavedStateHandle(),
                )
            }
        }
    }
}
