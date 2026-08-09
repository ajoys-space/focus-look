package com.focuslock.app.util

/**
 * App-wide constants. Centralizing these avoids magic numbers/strings scattered
 * across services, ViewModels, and Compose screens — especially important for
 * timer/limit values that multiple layers (Service, Repository, UI) all need
 * to agree on.
 */
object Constants {

    // DataStore / Room database names
    const val DATABASE_NAME = "focus_lock_db"
    const val PREFERENCES_NAME = "focus_lock_prefs"

    // Foreground service notification channel (needed from Step 11 onward)
    const val USAGE_TRACKING_CHANNEL_ID = "usage_tracking_channel"
    const val USAGE_TRACKING_NOTIFICATION_ID = 1001

    // Default unlock durations (in minutes) — user can override per-app later
    val DEFAULT_UNLOCK_DURATIONS_MINUTES = listOf(5, 10, 15)

    // Default daily limit options (in minutes)
    val DEFAULT_LIMIT_OPTIONS_MINUTES = listOf(30, 60, 120)

    // How often the usage-tracking service polls foreground app state
    const val USAGE_POLL_INTERVAL_MS = 1000L
    const val PRECISION_POLL_INTERVAL_MS = 200L
    const val PRECISION_THRESHOLD_MS = 30_000L // 30 seconds

    // Packages that should NOT trigger a session close (keyboard, shade, etc.)
    val SYSTEM_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.inputmethod.latin", // GBoard
        "com.android.inputmethod.latin",        // AOSP Keyboard
        "com.samsung.android.honeyboard",       // Samsung Keyboard
        "com.vivo.upslide",                     // Vivo Gesture System
        "com.vivo.systemuiplugin",              // Vivo UI Plugin
        "com.vivo.fingerprintui",               // Vivo Fingerprint
        "com.vivo.nightpearl",                  // Vivo AOD
        "com.vivo.card"                         // Vivo Smart Cards
    )
}