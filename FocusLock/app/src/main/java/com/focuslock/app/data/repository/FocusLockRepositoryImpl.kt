package com.focuslock.app.data.repository

import androidx.datastore.preferences.core.edit
import com.focuslock.app.data.local.dao.LockedAppDao
import com.focuslock.app.data.local.dao.StreakDao
import com.focuslock.app.data.local.dao.UnlockEventDao
import com.focuslock.app.data.local.dao.UsageSessionDao
import com.focuslock.app.data.local.datastore.PreferencesKeys
import com.focuslock.app.data.local.datastore.dataStore
import com.focuslock.app.data.model.DailyStreakEntity
import com.focuslock.app.data.model.LockedAppEntity
import com.focuslock.app.data.model.ThemeMode
import com.focuslock.app.data.model.UnlockEventEntity
import com.focuslock.app.data.model.UsageSessionEntity
import com.focuslock.app.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusLockRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val lockedAppDao: LockedAppDao,
    private val usageSessionDao: UsageSessionDao,
    private val unlockEventDao: UnlockEventDao,
    private val streakDao: StreakDao
) : FocusLockRepository {

    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    // ---- Locked Apps ----
    override fun getAllLockedApps(): Flow<List<LockedAppEntity>> = lockedAppDao.getAllLockedApps()
        .catch { emit(emptyList()) }

    override fun getAllEnabledLockedApps(): Flow<List<LockedAppEntity>> =
        lockedAppDao.getAllEnabledLockedApps()
            .catch { emit(emptyList()) }

    override suspend fun getLockedApp(packageName: String): LockedAppEntity? = try {
        lockedAppDao.getByPackageName(packageName)
    } catch (e: Exception) {
        null
    }

    override suspend fun addOrUpdateLockedApp(app: LockedAppEntity) = try {
        lockedAppDao.insert(app)
    } catch (e: Exception) {
    }

    override suspend fun removeLockedApp(packageName: String) = try {
        lockedAppDao.deleteByPackageName(packageName)
    } catch (e: Exception) {
    }

    override suspend fun isAppLocked(packageName: String): Boolean = try {
        lockedAppDao.isAppLocked(packageName)
    } catch (e: Exception) {
        false
    }

    override suspend fun getAllEnabledPackages(): List<String> = try {
        lockedAppDao.getAllEnabledLockedApps().first().map { it.packageName }
    } catch (e: Exception) {
        emptyList()
    }

    // ---- Usage Sessions ----
    override suspend fun recordUsageSession(session: UsageSessionEntity): Long = try {
        usageSessionDao.insert(session)
    } catch (e: Exception) {
        -1L
    }

    override suspend fun updateUsageSession(session: UsageSessionEntity) = try {
        usageSessionDao.update(session)
    } catch (e: Exception) {
    }

    override suspend fun getTodayUsageMillis(packageName: String): Long = try {
        usageSessionDao.getTotalUsageMillisForAppOnDay(packageName, todayEpochDay())
    } catch (e: Exception) {
        0L
    }

    override suspend fun getTodayUsageMillisExcludingSession(packageName: String, excludeId: Long): Long = try {
        usageSessionDao.getTotalUsageMillisForAppOnDayExcludingSession(packageName, todayEpochDay(), excludeId)
    } catch (e: Exception) {
        0L
    }

    override fun getTodayTotalUsageMillis(): Flow<Long> =
        usageSessionDao.getTotalUsageMillisForDay(todayEpochDay())
            .catch { emit(0L) }

    override fun getSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long
    ): Flow<List<UsageSessionEntity>> =
        usageSessionDao.getSessionsBetweenDates(startEpochDay, endEpochDay)
            .catch { emit(emptyList()) }

    // ---- Unlock Events ----
    override suspend fun recordUnlockEvent(event: UnlockEventEntity) = try {
        unlockEventDao.insert(event)
    } catch (e: Exception) {
    }

    override suspend fun getMostRecentUnlock(packageName: String): UnlockEventEntity? = try {
        unlockEventDao.getMostRecentUnlockForApp(packageName)
    } catch (e: Exception) {
        null
    }

    override fun getRecentUnlocks(sinceMillis: Long): Flow<List<UnlockEventEntity>> =
        unlockEventDao.getRecentUnlocks(sinceMillis)
            .catch { emit(emptyList()) }

    override fun getTodayUnlockCount(): Flow<Int> =
        unlockEventDao.getUnlockCountForDay(todayEpochDay())
            .catch { emit(0) }

    override suspend fun getActiveUnlockExpiryMillis(packageName: String): Long? = try {
        val recent = unlockEventDao.getMostRecentUnlockForApp(packageName)
        if (recent != null) {
            val expiresAt = recent.unlockedAtMillis + (recent.unlockDurationMinutes * 60_000L)
            if (System.currentTimeMillis() < expiresAt) expiresAt else null
        } else null
    } catch (e: Exception) {
        null
    }

    // ---- Streaks ----
    override suspend fun getCurrentStreak(): Int = try {
        val days = streakDao.getAllStreakDays().first()
        var streak = 0
        var expectedDay = todayEpochDay()
        for (day in days) {
            if (day.dateEpochDay == expectedDay && day.staysWithinLimits) {
                streak++
                expectedDay--
            } else if (day.dateEpochDay > expectedDay) {
                continue // Skip future days if they somehow exist
            } else {
                break
            }
        }
        streak
    } catch (e: Exception) {
        0
    }

    override suspend fun recomputeTodayStreakStatus() = try {
        val today = todayEpochDay()
        val enabledApps = lockedAppDao.getAllEnabledLockedApps().first()

        var totalMinutesToday = 0
        var withinAllLimits = true

        for (app in enabledApps) {
            val usedMillis = usageSessionDao.getTotalUsageMillisForAppOnDay(app.packageName, today)
            val usedMinutes = (usedMillis / 60000).toInt()
            totalMinutesToday += usedMinutes
            if (usedMinutes > app.dailyLimitMinutes) {
                withinAllLimits = false
            }
        }

        streakDao.upsert(
            DailyStreakEntity(
                dateEpochDay = today,
                staysWithinLimits = withinAllLimits,
                totalUsageMinutes = totalMinutesToday
            )
        )
    } catch (e: Exception) {
    }

    // ---- Preferences ----
    override val userPreferences: Flow<UserPreferences> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                UserPreferences(
                    onboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                    permissionsSeen = prefs[PreferencesKeys.PERMISSIONS_SEEN] ?: false,
                    themeMode = when (prefs[PreferencesKeys.THEME_MODE]) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    },
                    notificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
                )
            }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setPermissionsSeen(seen: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PERMISSIONS_SEEN] = seen }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    // ---- Sync tracking ----
    override suspend fun getLastUsageSyncMillis(): Long {
        val prefs = context.dataStore.data.first()
        return prefs[PreferencesKeys.LAST_USAGE_SYNC_MILLIS] ?: 0L
    }

    override suspend fun setLastUsageSyncMillis(millis: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_USAGE_SYNC_MILLIS] = millis }
    }
}