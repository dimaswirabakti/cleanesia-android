package com.cleanesia.feature.report.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cleanesia.ui.components.PlaceholderScreen
import com.cleanesia.ui.theme.CleanesiaTheme

class ReportDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanesiaTheme {
                PlaceholderScreen(title = "Laporkan Sampah")
            }
        }
    }
}
