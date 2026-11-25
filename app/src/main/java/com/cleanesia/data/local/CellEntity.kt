package com.cleanesia.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType

/** Ringkasan per sel peta. Isinya selalu mengikuti laporan terbaru di sel tersebut. */
@Entity(tableName = "cells")
data class CellEntity(
    @PrimaryKey val cellId: String,
    val latestSeverity: Severity,
    /** Epoch milidetik laporan terbaru. */
    val latestReportAt: Long,
    val types: List<WasteType>,
)
