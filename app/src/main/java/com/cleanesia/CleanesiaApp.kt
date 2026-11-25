package com.cleanesia

import android.app.Application
import com.cleanesia.data.local.AppDatabase
import com.cleanesia.data.repository.ReportRepository

/** Container dependensi manual untuk seluruh aplikasi. */
class CleanesiaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val reportRepository: ReportRepository by lazy { ReportRepository(database) }
}
