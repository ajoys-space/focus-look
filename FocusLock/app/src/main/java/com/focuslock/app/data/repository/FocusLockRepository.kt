package com.focuslock.app.data.repository

import com.focuslock.app.data.model.LockedAppEntity
import com.focuslock.app.data.model.ThemeMode
import com.focuslock.app.data.model.UnlockEventEntity
import com.focuslock.app.data.model.UsageSessionEntity
import com.focuslock.app.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for all app data — every ViewModel, Service, and
 * Worker depends on this interface, never on Room or DataStore directly.
 */
interface FocusLockRepository {

    // ---- Locked Apps ----
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>
    fun getAllEnabledLockedApps(): Flow<List<LockedAppEntity>>
    suspend fun getLockedApp(packageName: String): LockedAppEntity?
    suspend fun addOrUpdateLockedApp(app: LockedAppEntity)
    suspend fun removeLockedApp(packageName: String)
    suspend fun isAppLocked(packageName: String): Boolean
    suspend fun getAllEnabledPackages(): List<String>

    // ---- Usage Sessions ----
    suspend fun recordUsageSession(session: UsageSessionEntity): Long
    suspend fun updateUsageSession(session: UsageSessionEntity)
    suspend fun getTodayUsageMillis(packageName: String): Long
    suspend fun getTodayUsageMillisExcludingSession(packageName: String, excludeId: Long): Long
    fun getTodayTotalUsageMillis(): Flow<Long>
    fun getSessionsBetweenDates(startEpochDay: Long, endEpochDay: Long): Flow<List<UsageSessionEntity>>

    // ---- Unlock Events ----
    suspend fun recordUnlockEvent(event: UnlockEventEntity)
    suspend fun getMostRecentUnlock(packageName: String): UnlockEventEntity?
    fun getRecentUnlocks(sinceMillis: Long): Flow<List<UnlockEventEntity>>
    fun getTodayUnlockCount(): Flow<Int>

    /**
     * Returns the timestamp (epoch millis) at which the app's current
     * unlock window expires, or null if there is no active unlock right
     * now. This is the single formalized source of truth for "is this app
     * currently unlocked, and for how much longer" — used by the
     * Accessibility Service, Home screen, and Blocking screen alike.
     */
    suspend fun getActiveUnlockExpiryMillis(packageName: String): Long?

    // ---- Streaks ----
    suspend fun getCurrentStreak(): Int
    suspend fun recomputeTodayStreakStatus()

    // ---- Preferences ----
    val userPreferences: Flow<UserPreferences>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setPermissionsSeen(seen: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setNotificationsEnabled(enabled: Boolean)

    // ---- Sync tracking (used by UsageTrackingWorker to avoid duplicates) ----
    suspend fun getLastUsageSyncMillis(): Long
    suspend fun setLastUsageSyncMillis(millis: Long)
}