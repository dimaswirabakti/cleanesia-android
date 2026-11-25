package com.cleanesia.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType

@Entity(tableName = "reports", indices = [Index("cellId")])
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val types: List<WasteType>,
    val severity: Severity,
    val lat: Double,
    val lng: Double,
    val cellId: String,
    /** Epoch milidetik. */
    val createdAt: Long,
    /** Path absolut foto di penyimpanan internal aplikasi. */
    val photoPath: String?,
)
