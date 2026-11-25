package com.cleanesia.feature.report.done

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cleanesia.feature.main.MainActivity
import com.cleanesia.ui.theme.CleanesiaTheme

class ReportDoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanesiaTheme {
                ReportDoneScreen(onBackToHome = ::backToMain)
            }
        }
    }

    /**
     * CLEAR_TOP membuang semua Activity di atas MainActivity (Photo, dan Done ini),
     * SINGLE_TOP memakai instance MainActivity yang sudah ada sehingga tab yang aktif tidak di-reset.
     */
    private fun backToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }
}
