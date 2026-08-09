package com.focuslock.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Centralizes every DataStore key in one place, same rationale as Routes.kt —
 * prevents typo bugs where one file writes "theme_mode" and another reads
 * "themeMode" and silently gets a default value forever.
 */
object PreferencesKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val PERMISSIONS_SEEN = booleanPreferencesKey("permissions_seen")
    val THEME_MODE = stringPreferencesKey("theme_mode")           // "light" | "dark" | "system"
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val LAST_STREAK_CHECK_EPOCH_DAY = stringPreferencesKey("last_streak_check_epoch_day")
    val LAST_USAGE_SYNC_MILLIS = androidx.datastore.preferences.core.longPreferencesKey("last_usage_sync_millis")
}

/**
 * Creates a single DataStore instance tied to the Application Context.
 * The `by preferencesDataStore(...)` delegate guarantees only one instance
 * ever exists for this file name, even if called from multiple places —
 * critical since DataStore explicitly warns against creating multiple
 * instances pointed at the same file (causes data corruption).
 */
val Context.dataStore by preferencesDataStore(name = "focus_lock_prefs")