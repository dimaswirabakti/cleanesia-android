package com.cleanesia.feature.report.photo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cleanesia.feature.report.ReportExtras
import com.cleanesia.feature.report.detail.ReportDetailActivity
import com.cleanesia.ui.theme.CleanesiaTheme

class ReportPhotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanesiaTheme {
                ReportPhotoScreen(
                    onBack = { finish() },
                    onPhotoTaken = { path ->
                        startActivity(
                            Intent(this, ReportDetailActivity::class.java)
                                .putExtra(ReportExtras.PHOTO_PATH, path)
                        )
                    },
                )
            }
        }
    }
}
