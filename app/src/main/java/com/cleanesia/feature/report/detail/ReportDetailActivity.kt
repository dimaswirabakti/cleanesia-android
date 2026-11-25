package com.cleanesia.feature.report.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.cleanesia.feature.report.done.ReportDoneActivity
import com.cleanesia.ui.theme.CleanesiaTheme

class ReportDetailActivity : ComponentActivity() {

    private val viewModel: ReportDetailViewModel by viewModels { ReportDetailViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanesiaTheme {
                ReportDetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onSent = {
                        startActivity(Intent(this, ReportDoneActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}
