package com.yourname.challengetracker.data.model

import kotlinx.serialization.Serializable

/**
 * Serializable data classes for JSON persistence.
 * These are separate from domain models to allow for different representations.
 */

@Serializable
data class JournalEntryData(
    val date: String,
    val title: String,
    val content: String,
    val audioPath: String? = null,
    val isTranscribed: Boolean = false,
    val isEditable: Boolean = true
)

@Serializable
data class ColoredDayData(
    val date: String,
    val colorHex: String
)

@Serializable
data class AppData(
    val journalEntries: List<JournalEntryData> = emptyList(),
    val coloredDays: List<ColoredDayData> = emptyList(),
    val colorIntensity: Float = 1.0f
)
