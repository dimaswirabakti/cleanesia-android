package com.cleanesia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cleanesia.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val Default = Typography()

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 33.6.sp,
    ),
    displayMedium = Default.displayMedium.copy(fontFamily = Poppins),
    displaySmall = Default.displaySmall.copy(fontFamily = Poppins),
    headlineLarge = Default.headlineLarge.copy(fontFamily = Poppins),
    headlineMedium = Default.headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = Default.headlineSmall.copy(fontFamily = Poppins),
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = Default.titleSmall.copy(fontFamily = Poppins),
    bodyLarge = Default.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = Default.labelLarge.copy(fontFamily = Poppins),
    labelMedium = Default.labelMedium.copy(fontFamily = Poppins),
    labelSmall = Default.labelSmall.copy(fontFamily = Poppins),
)
