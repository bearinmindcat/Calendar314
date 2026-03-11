package com.yourname.challengetracker.MainScreens.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Adjusts color intensity based on theme mode.
 * In dark mode, mixes with dark background.
 * In light mode, mixes with white.
 */
fun adjustColorIntensity(color: Color, intensity: Float, isDarkTheme: Boolean): Color {
    val argb = color.toArgb()
    val alpha = (argb shr 24) and 0xFF
    val red = (argb shr 16) and 0xFF
    val green = (argb shr 8) and 0xFF
    val blue = argb and 0xFF

    if (isDarkTheme) {
        // In dark mode, mix with dark background for better visibility
        val bgValue = 18 // Dark background value
        val mixedRed = (red * intensity + bgValue * (1 - intensity)).toInt()
        val mixedGreen = (green * intensity + bgValue * (1 - intensity)).toInt()
        val mixedBlue = (blue * intensity + bgValue * (1 - intensity)).toInt()
        return Color((alpha shl 24) or (mixedRed shl 16) or (mixedGreen shl 8) or mixedBlue)
    } else {
        // In light mode, mix with white
        val mixedRed = (red * intensity + 255 * (1 - intensity)).toInt()
        val mixedGreen = (green * intensity + 255 * (1 - intensity)).toInt()
        val mixedBlue = (blue * intensity + 255 * (1 - intensity)).toInt()
        return Color((alpha shl 24) or (mixedRed shl 16) or (mixedGreen shl 8) or mixedBlue)
    }
}
