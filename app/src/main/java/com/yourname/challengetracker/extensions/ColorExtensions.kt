package com.yourname.challengetracker.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Converts a Color to its hex string representation (8 characters including alpha).
 * Example: Color.Red -> "FFFF0000"
 */
fun Color.toHexString(): String {
    val argb = this.toArgb()
    return String.format("%08X", argb)
}

/**
 * Converts a hex string to a Color.
 * Supports both 6-character (RGB) and 8-character (ARGB) hex strings.
 * Example: "FFFF0000" -> Color.Red
 */
fun String.toColor(): Color {
    return try {
        val colorInt = this.toLong(16).toInt()
        Color(colorInt)
    } catch (e: NumberFormatException) {
        Color.Transparent
    }
}

/**
 * Converts a hex string to a Color, or returns null if parsing fails.
 */
fun String.toColorOrNull(): Color? {
    return try {
        val colorInt = this.toLong(16).toInt()
        Color(colorInt)
    } catch (e: NumberFormatException) {
        null
    }
}
