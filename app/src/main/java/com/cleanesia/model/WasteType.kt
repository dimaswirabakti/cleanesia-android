package com.cleanesia.model

/** Kategori sampah. Urutan indeks tetap: 0 Plastik, 1 Styrofoam, 2 Logam, 3 Kaca, 4 Jaring. */
enum class WasteType(val index: Int, val label: String) {
    PLASTIK(0, "Plastik"),
    STYROFOAM(1, "Styrofoam"),
    LOGAM(2, "Logam"),
    KACA(3, "Kaca"),
    JARING(4, "Jaring");

    companion object {
        fun fromIndex(index: Int): WasteType = entries.first { it.index == index }
    }
}
