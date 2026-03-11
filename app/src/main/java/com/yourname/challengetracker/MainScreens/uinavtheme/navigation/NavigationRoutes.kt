package com.yourname.challengetracker.MainScreens.uinavtheme.navigation

/**
 * Sealed class defining all navigation routes in the app.
 * Provides type-safe navigation instead of hardcoded strings.
 */
sealed class NavigationRoutes(val route: String) {
    object Calendar : NavigationRoutes("calendar")
    object JournalList : NavigationRoutes("journal_list")
    object Settings : NavigationRoutes("settings")

    object JournalEntry : NavigationRoutes("journal_entry/{date}") {
        fun createRoute(date: String) = "journal_entry/$date"
        const val DATE_ARG = "date"
    }
}
