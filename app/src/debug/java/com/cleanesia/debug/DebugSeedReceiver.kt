package com.cleanesia.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cleanesia.CleanesiaApp
import com.cleanesia.data.repository.ReportRepository
import com.cleanesia.model.Severity
import com.cleanesia.model.WasteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Pengisi data uji, hanya ada di build debug.
 *
 * adb shell am broadcast -a com.cleanesia.debug.SEED -n com.cleanesia/.debug.DebugSeedReceiver
 * adb shell am broadcast -a com.cleanesia.debug.CLEAR -n com.cleanesia/.debug.DebugSeedReceiver
 */
class DebugSeedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as CleanesiaApp
        val pending = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_SEED -> {
                        app.database.clearAllTables()
                        seed(app.reportRepository)
                        Log.i(TAG, "Seed selesai")
                    }
                    ACTION_CLEAR -> {
                        app.database.clearAllTables()
                        Log.i(TAG, "Data dihapus")
                    }
                    else -> Log.w(TAG, "Aksi tidak dikenal: ${intent.action}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal: ${intent.action}", e)
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * Data di sekitar Pantai Parangtritis. Tiap kasus menguji satu aturan peta:
     * warna per level, laporan terbaru menang, urutan insert, pudar, basi, dan batas sel.
     */
    private suspend fun seed(repo: ReportRepository) {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour

        suspend fun add(lat: Double, lng: Double, severity: Severity, ageMs: Long, vararg types: WasteType) {
            repo.addReport(types.toList(), severity, lat, lng, now - ageMs, photoPath = null)
        }

        // Baris lat -8.0255 (sel -8026_*): lima level keparahan, umur 0 hari.
        val levels = Severity.entries
        levels.forEachIndexed { i, severity ->
            add(-8.0255, 110.3285 + i * 0.0012, severity, i * minute, WasteType.PLASTIK)
        }

        // Laporan terbaru menang: lama Sangat Tinggi, baru Rendah. Hasil: Rendah.
        add(-8.0235, 110.3295, Severity.SANGAT_TINGGI, 2 * hour, WasteType.JARING)
        add(-8.0235, 110.3295, Severity.RENDAH, 1 * hour, WasteType.PLASTIK, WasteType.KACA)

        // Urutan insert terbalik: yang baru dimasukkan dulu. Hasil tetap Sedang.
        add(-8.0235, 110.3315, Severity.SEDANG, 1 * hour, WasteType.STYROFOAM)
        add(-8.0235, 110.3315, Severity.TINGGI, 3 * hour, WasteType.LOGAM)

        // Umur 10 hari: warna lebih pudar.
        add(-8.0235, 110.3335, Severity.TINGGI, 10 * day, WasteType.PLASTIK)

        // Umur 25 hari: lewat ambang 21 hari, jadi abu-abu.
        add(-8.0235, 110.3355, Severity.SANGAT_TINGGI, 25 * day, WasteType.PLASTIK)

        // Tepat di batas: (-8.025, 110.332) masuk sel -8025_110331 sesuai fixture.
        add(-8.025, 110.332, Severity.SEDANG, 30 * minute, WasteType.KACA)
    }

    private companion object {
        const val TAG = "DebugSeed"
        const val ACTION_SEED = "com.cleanesia.debug.SEED"
        const val ACTION_CLEAR = "com.cleanesia.debug.CLEAR"
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
