package com.cleanesia.feature.report.photo

import android.Manifest
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleanesia.R
import com.cleanesia.core.location.hasLocationPermission
import com.cleanesia.ui.theme.Background
import com.cleanesia.ui.theme.Brand700
import kotlinx.coroutines.launch

/** Warna isi tombol "Ambil Foto", diambil dari desain (design-ref/Foto laporan sampah.png). */
private val TakePhotoButtonContainer = Color(0xFFF0F5F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPhotoScreen(
    onBack: () -> Unit,
    onPhotoTaken: (photoPath: String) -> Unit,
    viewModel: ReportPhotoViewModel = viewModel(factory = ReportPhotoViewModel.Factory),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onPhotoResult(success)?.let(onPhotoTaken)
    }

    fun launchCamera() {
        val file = viewModel.newPhotoFile()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        try {
            takePicture.launch(uri)
        } catch (_: ActivityNotFoundException) {
            viewModel.onPhotoResult(success = false)
            scope.launch { snackbarHostState.showSnackbar("Aplikasi kamera tidak ditemukan.") }
        }
    }

    // Lokasi diambil saat foto diambil, jadi izinnya diminta di sini bila belum ada.
    // Apa pun jawabannya, kamera tetap dibuka; laporan tertahan di Detail sampai lokasi didapat.
    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { launchCamera() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foto Laporan Sampah") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    scrolledContainerColor = Background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            ElevatedButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        launchCamera()
                    } else {
                        requestLocation.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    }
                },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TakePhotoButtonContainer,
                    contentColor = Brand700,
                ),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    painterResource(R.drawable.ic_nav_camera),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Ambil Foto")
            }
        }
    }
}
