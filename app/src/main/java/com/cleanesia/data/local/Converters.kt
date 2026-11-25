package com.cleanesia.data.local

import androidx.room.TypeConverter
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType

/** Jenis sampah disimpan sebagai indeks berurutan dipisah koma, contoh "0,3". */
class Converters {
    @TypeConverter
    fun wasteTypesToString(types: List<WasteType>): String =
        types.sortedBy { it.index }.joinToString(",") { it.index.toString() }

    @TypeConverter
    fun stringToWasteTypes(value: String): List<WasteType> =
        if (value.isEmpty()) emptyList() else value.split(",").map { WasteType.fromIndex(it.toInt()) }

    @TypeConverter
    fun severityToLevel(severity: Severity): Int = severity.level

    @TypeConverter
    fun levelToSeverity(level: Int): Severity = Severity.fromLevel(level)
}
