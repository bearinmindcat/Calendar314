package com.yourname.challengetracker

import android.app.Application

/**
 * Application class for ChallengeTracker.
 * Can be extended for app-wide initialization like dependency injection,
 * crash reporting, or logging frameworks.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components here
    }
}
