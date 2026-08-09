package com.focuslock.app.ui.screens.home

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.service.AppBlockerAccessibilityService
import com.focuslock.app.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val usageMillis: Long,
    val limitMinutes: Int,
    val isUnlocked: Boolean
)

data class HomeUiState(
    val todayUsageMinutes: Int = 0,
    val totalDailyLimitMinutes: Int = 0,
    val lockedAppsCount: Int = 0,
    val unlockedAppsCount: Int = 0,
    val currentStreak: Int = 0,
    val trackedApps: List<AppUsageItem> = emptyList(),
    val isLoading: Boolean = true,
    val isCriticalPermissionMissing: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FocusLockRepository,
    application: Application
) : AndroidViewModel(application) {

    private val today = LocalDate.now().toEpochDay()
    private val iconCache = mutableMapOf<String, ImageBitmap>()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllEnabledLockedApps(),
        repository.getTodayTotalUsageMillis(),
        repository.getSessionsBetweenDates(today, today),
        repository.getRecentUnlocks(System.currentTimeMillis() - 2 * 60 * 60 * 1000)
    ) { lockedApps, todayTotalUsageMillis, todaySessions, recentUnlocks ->
        try {
            val pm = getApplication<Application>().packageManager
            val streak = repository.getCurrentStreak()
            val permissionsMissing = !checkCriticalPermissions()
            val totalLimit = lockedApps.sumOf { it.dailyLimitMinutes }

            val now = System.currentTimeMillis()
            
            // Optimization: Calculate maps once instead of inside the loop
            val usageMap = todaySessions.groupBy { it.packageName }
                .mapValues { (_, sessions) -> sessions.sumOf { it.durationMillis } }
            
            val unlockedSet = lockedApps.filter { app ->
                recentUnlocks
                    .filter { it.packageName == app.packageName }
                    .any { it.unlockedAtMillis + (it.unlockDurationMinutes * 60_000L) > now }
            }.map { it.packageName }.toSet()

            val trackedApps = lockedApps.map { app ->
                val usageMillis = usageMap[app.packageName] ?: 0L
                val isUnlocked = app.packageName in unlockedSet

                // Cache the icon to prevent scroll lag
                val cachedIcon = iconCache[app.packageName] ?: try {
                    val bitmap = pm.getApplicationIcon(app.packageName).toBitmap().asImageBitmap()
                    iconCache[app.packageName] = bitmap
                    bitmap
                } catch (e: Exception) {
                    null
                }

                AppUsageItem(
                    packageName = app.packageName,
                    appName = app.appName,
                    icon = cachedIcon,
                    usageMillis = usageMillis,
                    limitMinutes = app.dailyLimitMinutes,
                    isUnlocked = isUnlocked
                )
            }.sortedByDescending { it.usageMillis }

            HomeUiState(
                todayUsageMinutes = (todayTotalUsageMillis / 60000).toInt(),
                totalDailyLimitMinutes = totalLimit,
                lockedAppsCount = lockedApps.size,
                unlockedAppsCount = trackedApps.count { it.isUnlocked },
                currentStreak = streak,
                trackedApps = trackedApps,
                isLoading = false,
                isCriticalPermissionMissing = permissionsMissing
            )
        } catch (e: Exception) {
            // Fallback state on error to prevent ANR/Crash
            HomeUiState(isLoading = false)
        }
    }
    .flowOn(Dispatchers.Default) // Critical fix for ANR: run logic off UI thread
    .catch { emit(HomeUiState(isLoading = false)) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun checkCriticalPermissions(): Boolean {
        val context = getApplication<Application>()
        val usageStats = PermissionUtils.hasUsageStatsPermission(context)
        val accessibility = PermissionUtils.isAccessibilityServiceEnabled(
            context,
            AppBlockerAccessibilityService::class.java
        )
        val overlay = PermissionUtils.hasOverlayPermission(context)
        return usageStats && accessibility && overlay
    }
}
