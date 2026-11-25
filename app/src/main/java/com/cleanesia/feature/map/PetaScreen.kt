package com.cleanesia.feature.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cleanesia.core.geo.computeCellId
import com.cleanesia.core.location.currentLocationOrNull
import com.cleanesia.data.local.CellEntity
import com.cleanesia.ui.components.CellMap
import com.cleanesia.ui.theme.Background
import com.cleanesia.ui.theme.Ink600
import com.cleanesia.ui.theme.Surface
import org.osmdroid.util.GeoPoint

/** Hasil ketukan di peta. [cell] null berarti sel yang diketuk belum punya laporan. */
private data class CellTap(val cell: CellEntity?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(viewModel: PetaViewModel = viewModel(factory = PetaViewModel.Factory)) {
    val cells by viewModel.cells.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var tapped by remember { mutableStateOf<CellTap?>(null) }

    // Izin lokasi diminta di Landing. Di sini hanya dipakai kalau sudah diberikan.
    LaunchedEffect(Unit) {
        currentLocationOrNull(context)?.let { userLocation = GeoPoint(it.latitude, it.longitude) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Peta Sebaran") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    scrolledContainerColor = Background,
                ),
            )
        },
    ) { innerPadding ->
        CellMap(
            cells = cells,
            userLocation = userLocation,
            initialCamera = viewModel.camera,
            onCameraSave = { viewModel.camera = it },
            onTap = { point ->
                val cellId = computeCellId(point.latitude, point.longitude)
                tapped = CellTap(cells.firstOrNull { it.cellId == cellId })
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }

    tapped?.let { tap ->
        ModalBottomSheet(
            onDismissRequest = { tapped = null },
            containerColor = Surface,
            dragHandle = null,
        ) {
            CellDetail(cell = tap.cell)
        }
    }
}

@Composable
private fun CellDetail(cell: CellEntity?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
    ) {
        if (cell == null) {
            Text("Belum terpantau di sel ini.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }
        Text(
            text = "Keparahan: ${cell.latestSeverity.label}",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Jenis: ${cell.types.joinToString(", ") { it.label }}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Terakhir dilaporkan: ${timeAgo(cell.latestReportAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = Ink600,
        )
    }
}

/** Sama dengan _ago di home_map_screen.dart. */
private fun timeAgo(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - epochMillis) / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "$days hari lalu"
        hours > 0 -> "$hours jam lalu"
        else -> "$minutes menit lalu"
    }
}
