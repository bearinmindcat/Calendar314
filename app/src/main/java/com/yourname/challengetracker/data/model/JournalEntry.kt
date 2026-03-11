package com.yourname.challengetracker.data.model

import java.time.LocalDate

/**
 * Domain model for journal entries.
 * Used throughout the app for displaying and manipulating journal data.
 */
data class JournalEntry(
    val date: LocalDate,
    val title: String,
    val content: String,
    val audioPath: String? = null,
    val isTranscribed: Boolean = false,
    val isEditable: Boolean = true
)
