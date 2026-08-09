package com.focuslock.app.data.model

/** Represents theme choice per your spec's Settings screen (Dark/Light/Material You). */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/**
 * Plain data class the UI layer actually consumes — keeps DataStore's raw
 * Preferences object (and its string-based keys) fully hidden behind the
 * Repository, so screens never touch DataStore APIs directly.
 */
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val permissionsSeen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true
)