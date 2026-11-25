package com.cleanesia.ui.components

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cleanesia.R
import com.cleanesia.core.geo.STALE_THRESHOLD_DAYS
import com.cleanesia.core.geo.cellBounds
import com.cleanesia.data.local.CellEntity
import com.cleanesia.ui.theme.SeverityColors
import com.cleanesia.ui.theme.Unmonitored
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/** Titik cadangan bila lokasi pengguna tidak tersedia (sama dengan versi Flutter). */
private val FALLBACK_CENTER = GeoPoint(-8.025, 110.332)
private const val FALLBACK_ZOOM = 5.0
private const val USER_ZOOM = 16.0
private const val DAY_MS = 24L * 60 * 60 * 1000

/**
 * Peta OpenStreetMap dengan kotak sel berwarna sesuai keparahan laporan terbaru.
 * Ketukan di mana pun diteruskan ke [onTap] sebagai koordinat.
 */
@Composable
fun CellMap(
    cells: List<CellEntity>,
    userLocation: GeoPoint?,
    onTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnTap by rememberUpdatedState(onTap)
    val outlineWidthPx = with(LocalDensity.current) { 0.5.dp.toPx() }

    val cellLayer = remember { FolderOverlay() }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(FALLBACK_ZOOM)
            controller.setCenter(FALLBACK_CENTER)
            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        currentOnTap(p)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                })
            )
            overlays.add(cellLayer)
            // Atribusi wajib tampil di peta menurut kebijakan tile OSM.
            overlays.add(CopyrightOverlay(context).apply { setAlignRight(true) })
        }
    }
    val userMarker = remember {
        Marker(mapView).apply {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            infoWindow = null
            setOnMarkerClickListener { _, _ -> false }
        }
    }

    // MapView perlu tahu siklus hidup layar untuk menghentikan unduhan tile saat di latar.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            mapView.controller.setZoom(USER_ZOOM)
            mapView.controller.setCenter(userLocation)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            val now = System.currentTimeMillis()
            cellLayer.items.clear()
            cells.forEach { cell ->
                cellLayer.add(cellPolygon(map, cell, now, outlineWidthPx))
            }
            map.overlays.remove(userMarker)
            if (userLocation != null) {
                userMarker.position = userLocation
                map.overlays.add(userMarker)
            }
            map.invalidate()
        },
    )
}

private fun cellPolygon(map: MapView, cell: CellEntity, now: Long, outlineWidthPx: Float): Polygon {
    val b = cellBounds(cell.cellId)
    return Polygon(map).apply {
        setPoints(
            listOf(
                GeoPoint(b.latLo, b.lngLo),
                GeoPoint(b.latHi, b.lngLo),
                GeoPoint(b.latHi, b.lngHi),
                GeoPoint(b.latLo, b.lngHi),
            )
        )
        fillPaint.color = cellFillColor(cell, now).toArgb()
        outlinePaint.apply {
            color = Color.Black.copy(alpha = 0.26f).toArgb()
            strokeWidth = outlineWidthPx
            style = Paint.Style.STROKE
        }
        infoWindow = null
        // Kembalikan false supaya ketukan diteruskan ke MapEventsOverlay.
        setOnClickListener { _, _, _ -> false }
    }
}

/**
 * Warna sel: keparahan laporan terbaru, makin pudar seiring umur laporan.
 * Lebih tua dari [STALE_THRESHOLD_DAYS] hari menjadi abu-abu "belum terpantau".
 * Umur hanya mengubah kesegaran warna, tidak mengubah nilai keparahan.
 */
internal fun cellFillColor(cell: CellEntity, now: Long): Color {
    val ageDays = ((now - cell.latestReportAt) / DAY_MS).toInt()
    if (ageDays > STALE_THRESHOLD_DAYS) return Unmonitored.copy(alpha = 0.35f)
    val t = (ageDays.toFloat() / STALE_THRESHOLD_DAYS).coerceIn(0f, 1f)
    return SeverityColors[cell.latestSeverity.level - 1].copy(alpha = 0.70f - 0.45f * t)
}
