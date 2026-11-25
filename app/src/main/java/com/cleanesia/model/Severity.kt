package com.cleanesia.model

/** Tingkat keparahan ordinal, level 1 (sangat rendah) sampai 5 (sangat tinggi). */
enum class Severity(val level: Int, val label: String) {
    SANGAT_RENDAH(1, "Sangat Rendah"),
    RENDAH(2, "Rendah"),
    SEDANG(3, "Sedang"),
    TINGGI(4, "Tinggi"),
    SANGAT_TINGGI(5, "Sangat Tinggi");

    companion object {
        fun fromLevel(level: Int): Severity = entries.first { it.level == level }
    }
}
