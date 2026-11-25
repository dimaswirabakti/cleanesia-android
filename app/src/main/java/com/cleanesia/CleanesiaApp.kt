package com.cleanesia

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import com.cleanesia.data.local.AppDatabase
import com.cleanesia.data.repository.ReportRepository
import org.osmdroid.config.Configuration
import java.io.File

/** Container dependensi manual untuk seluruh aplikasi. */
class CleanesiaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val reportRepository: ReportRepository by lazy { ReportRepository(database) }

    override fun onCreate() {
        super.onCreate()
        configureOsmdroid()
    }

    /**
     * Kebijakan tile OpenStreetMap mewajibkan User-Agent unik yang menyebut nama aplikasi.
     * Bawaan osmdroid adalah "osmdroid", jadi harus diganti sebelum MapView pertama dibuat.
     * Cache tile disimpan di direktori cache aplikasi, tanpa izin penyimpanan eksternal.
     */
    private fun configureOsmdroid() {
        val base = File(cacheDir, "osmdroid")
        Configuration.getInstance().apply {
            userAgentValue = "Cleanesia/${versionName()} (Android; $packageName)"
            osmdroidBasePath = base
            osmdroidTileCache = File(base, "tiles")
        }
    }

    private fun versionName(): String {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return info.versionName ?: "dev"
    }
}
