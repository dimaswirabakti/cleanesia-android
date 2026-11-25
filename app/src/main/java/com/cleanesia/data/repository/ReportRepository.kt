package com.cleanesia.data.repository

import androidx.room.withTransaction
import com.cleanesia.core.geo.computeCellId
import com.cleanesia.core.geo.shouldReplaceCell
import com.cleanesia.data.local.AppDatabase
import com.cleanesia.data.local.CellEntity
import com.cleanesia.data.local.ReportEntity
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val db: AppDatabase) {
    private val reportDao = db.reportDao()
    private val cellDao = db.cellDao()

    val reports: Flow<List<ReportEntity>> = reportDao.observeAll()
    val cells: Flow<List<CellEntity>> = cellDao.observeAll()
    val reportCount: Flow<Int> = reportDao.observeCount()
    val cellCount: Flow<Int> = cellDao.observeCount()

    /**
     * Simpan laporan dan perbarui sel tempat laporan berada dalam satu transaksi.
     * Sel hanya diganti bila laporan ini lebih baru dari laporan terbaru sel tersebut.
     */
    suspend fun addReport(
        types: List<WasteType>,
        severity: Severity,
        lat: Double,
        lng: Double,
        createdAt: Long,
        photoPath: String?,
    ): Long = db.withTransaction {
        val cellId = computeCellId(lat, lng)
        val id = reportDao.insert(
            ReportEntity(
                types = types,
                severity = severity,
                lat = lat,
                lng = lng,
                cellId = cellId,
                createdAt = createdAt,
                photoPath = photoPath,
            )
        )
        val current = cellDao.get(cellId)
        if (shouldReplaceCell(current?.latestReportAt, createdAt)) {
            cellDao.upsert(
                CellEntity(
                    cellId = cellId,
                    latestSeverity = severity,
                    latestReportAt = createdAt,
                    types = types,
                )
            )
        }
        id
    }
}
