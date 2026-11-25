package com.cleanesia.feature.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cleanesia.ui.components.PlaceholderScreen
import com.cleanesia.ui.theme.CleanesiaTheme

class TentangActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanesiaTheme {
                PlaceholderScreen(title = "Tentang Aplikasi")
            }
        }
    }
}
