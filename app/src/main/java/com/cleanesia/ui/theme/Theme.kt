package com.cleanesia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanesiaColorScheme = lightColorScheme(
    primary = Brand500,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand900,
    secondary = Brand700,
    onSecondary = Color.White,
    secondaryContainer = Brand100,
    onSecondaryContainer = Ink900,
    background = Background,
    onBackground = Ink900,
    surface = Surface,
    onSurface = Ink900,
    surfaceVariant = Brand50,
    onSurfaceVariant = Ink600,
    surfaceContainer = Surface,
    outline = Line,
    outlineVariant = Line,
    error = Danger,
    onError = Color.White,
)

// Aplikasi hanya punya tema terang, tanpa dynamic color, supaya warna brand tetap konsisten.
@Composable
fun CleanesiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanesiaColorScheme,
        typography = Typography,
        content = content,
    )
}
