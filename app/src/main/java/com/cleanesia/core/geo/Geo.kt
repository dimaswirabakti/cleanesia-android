package com.cleanesia.core.geo

import kotlin.math.floor

/** Ukuran sel grid dalam derajat (~111 m pada arah lintang). */
const val CELL_SIZE_DEG = 0.001

/** Sel dianggap belum terpantau bila laporan terbarunya lebih tua dari ini. */
const val STALE_THRESHOLD_DAYS = 21

/**
 * Konversi koordinat ke id sel grid.
 * Contoh: (-8.0251, 110.3320) -> "-8026_110331".
 */
fun computeCellId(lat: Double, lng: Double): String {
    val latCell = floor(lat / CELL_SIZE_DEG).toLong()
    val lngCell = floor(lng / CELL_SIZE_DEG).toLong()
    return "${latCell}_$lngCell"
}

data class CellBounds(
    val latLo: Double,
    val latHi: Double,
    val lngLo: Double,
    val lngHi: Double,
) {
    val latMid: Double get() = (latLo + latHi) / 2
    val lngMid: Double get() = (lngLo + lngHi) / 2
}

/** Ubah id sel (contoh "-8026_110331") kembali menjadi kotak geografis. */
fun cellBounds(cellId: String): CellBounds {
    val (latPart, lngPart) = cellId.split("_")
    val latCell = latPart.toLong()
    val lngCell = lngPart.toLong()
    return CellBounds(
        latLo = latCell * CELL_SIZE_DEG,
        latHi = (latCell + 1) * CELL_SIZE_DEG,
        lngLo = lngCell * CELL_SIZE_DEG,
        lngHi = (lngCell + 1) * CELL_SIZE_DEG,
    )
}

/**
 * Sel hanya diganti oleh laporan yang lebih baru dari laporan terbaru yang tersimpan.
 * Laporan dengan waktu sama atau lebih lama tidak mengubah sel.
 */
fun shouldReplaceCell(currentLatestAt: Long?, incomingCreatedAt: Long): Boolean =
    currentLatestAt == null || incomingCreatedAt > currentLatestAt
