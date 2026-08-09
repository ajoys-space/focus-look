package com.focuslock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.focuslock.app.data.model.UsageSessionEntity
import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.ui.screens.blocking.BlockingActivity
import com.focuslock.app.util.Constants
import com.focuslock.app.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Reliability Overhaul v2: Heartbeat Persistence + Accurate Calculation.
 */
@AndroidEntryPoint
class AppBlockerAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var repository: FocusLockRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val stateMutex = Mutex()

    private var lastForegroundPackage: String? = null

    // Tracks the currently open live session
    private var currentSessionPackage: String? = null
    private var currentSessionStartMillis: Long = 0L
    private var activeSessionId: Long? = null
    
    private var pollingJob: Job? = null
    private var heartbeatJob: Job? = null

    private var restrictedPackagesSnapshot = emptyList<String>()

    companion object {
        private const val TAG = "AppBlockerService"
        private const val HEARTBEAT_INTERVAL_MS = 15000L // Persist to DB every 15s
        private const val RECOVERY_LOOKBACK_MS = 5 * 60 * 1000L // 5 minutes
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "[SYSTEM] Accessibility service connected")

        serviceScope.launch {
            // Update restricted packages snapshot
            restrictedPackagesSnapshot = repository.getAllEnabledPackages()
            Log.d(TAG, "[SYSTEM] Currently monitoring: $restrictedPackagesSnapshot")

            val currentForeground = getForegroundPackageViaUsageStats()
            if (currentForeground != null && currentForeground != packageName) {
                Log.d(TAG, "[STATE] Recovering session for: $currentForeground")
                handleAppSwitch(currentForeground)
            }
        }
    }

    private fun getForegroundPackageViaUsageStats(): String? {
        val usageStatsManager = getSystemService(android.content.Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
        val now = System.currentTimeMillis()
        // Increase lookback to 5 minutes for better recovery
        val events = usageStatsManager?.queryEvents(now - RECOVERY_LOOKBACK_MS, now) ?: return null
        
        var lastPkg: String? = null
        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPkg = event.packageName
            }
        }
        return lastPkg
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString()
        if (packageName == null || packageName == lastForegroundPackage) return

        lastForegroundPackage = packageName
        Log.d(TAG, "[EVENT] Window changed to: $packageName")

        serviceScope.launch {
            handleAppSwitch(packageName)
        }
    }

    private suspend fun handleAppSwitch(newPackageName: String?) {
        stateMutex.withLock {
            // Check if this is a "System Transition" (Keyboard, Shade, etc.)
            if (newPackageName != null && Constants.SYSTEM_PACKAGES.contains(newPackageName)) {
                Log.d(TAG, "[DEBUG] Continuity maintained for system package: $newPackageName")
                return
            }

            // 1. Close current session
            closeCurrentSessionInternal()

            if (newPackageName == null || newPackageName == this.packageName) {
                Log.d(TAG, "[DEBUG] Ignoring system/own package: $newPackageName")
                return
            }

            // 2. Start new session if app is locked
            val lockedApp = repository.getLockedApp(newPackageName)
            
            if (lockedApp == null) {
                Log.d(TAG, "[DEBUG] $newPackageName is not restricted (not in DB)")
                return
            }
            
            if (!lockedApp.isEnabled) {
                Log.d(TAG, "[DEBUG] $newPackageName restriction is currently disabled")
                return
            }

            // 3. App is restricted and enabled
            currentSessionPackage = newPackageName
            currentSessionStartMillis = System.currentTimeMillis()
            
            // Create the session record immediately to get an ID for heartbeats
            activeSessionId = repository.recordUsageSession(
                UsageSessionEntity(
                    packageName = newPackageName,
                    startTimeMillis = currentSessionStartMillis,
                    endTimeMillis = currentSessionStartMillis,
                    durationMillis = 0,
                    dateEpochDay = LocalDate.now().toEpochDay()
                )
            )

            Log.d(TAG, "[STATE] Started live session for $newPackageName (ID: $activeSessionId)")

            // 4. Initial block check
            checkAndHandleForegroundAppInternal(newPackageName)

            // 5. Start polling and heartbeat
            startPollingInternal(newPackageName)
            activeSessionId?.let { startHeartbeatInternal(newPackageName, it) }
        }
    }

    private fun startPollingInternal(packageName: String) {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            var currentDelay = Constants.USAGE_POLL_INTERVAL_MS
            while (isActive) {
                delay(currentDelay)
                stateMutex.withLock {
                    if (currentSessionPackage == packageName) {
                        val isNearLimit = checkAndHandleForegroundAppInternal(packageName)
                        currentDelay = if (isNearLimit) {
                            Constants.PRECISION_POLL_INTERVAL_MS
                        } else {
                            Constants.USAGE_POLL_INTERVAL_MS
                        }
                    } else {
                        cancel()
                    }
                }
            }
        }
    }

    private fun startHeartbeatInternal(packageName: String, sessionId: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                stateMutex.withLock {
                    if (currentSessionPackage == packageName && activeSessionId == sessionId) {
                        updateActiveSessionInternal(packageName, sessionId)
                    } else {
                        cancel()
                    }
                }
            }
        }
    }

    private suspend fun updateActiveSessionInternal(packageName: String, sessionId: Long) {
        val now = System.currentTimeMillis()
        val duration = now - currentSessionStartMillis
        if (duration <= 0) return

        repository.updateUsageSession(
            UsageSessionEntity(
                id = sessionId,
                packageName = packageName,
                startTimeMillis = currentSessionStartMillis,
                endTimeMillis = now,
                durationMillis = duration,
                dateEpochDay = LocalDate.now().toEpochDay()
            )
        )
        Log.d(TAG, "[STATE] Heartbeat persistent save for $packageName: ${duration / 1000}s")
    }

    private suspend fun checkAndHandleForegroundAppInternal(packageName: String): Boolean {
        if (!::repository.isInitialized) {
            Log.w(TAG, "[DEBUG] Repository NOT INITIALIZED yet for $packageName")
            return false
        }

        val lockedApp = repository.getLockedApp(packageName) ?: run {
            Log.d(TAG, "[DEBUG] $packageName not in DB")
            return false
        }
        
        if (!lockedApp.isEnabled) {
            Log.d(TAG, "[DEBUG] $packageName disabled")
            return false
        }

        // MATH FIX: Sum all historical sessions EXCEPT the one currently active.
        val historicalUsageMillis = repository.getTodayUsageMillisExcludingSession(
            packageName, 
            activeSessionId ?: -1
        )
        
        val liveSessionMillis = if (currentSessionPackage == packageName) {
            System.currentTimeMillis() - currentSessionStartMillis
        } else 0L

        val totalUsageMillis = historicalUsageMillis + liveSessionMillis
        val limitMillis = lockedApp.dailyLimitMinutes * 60_000L

        val isCurrentlyUnlocked = repository.getActiveUnlockExpiryMillis(packageName) != null

        // High-precision timing log
        val msMarker = System.currentTimeMillis() % 100000
        Log.d(TAG, "[MATH][$msMarker] $packageName -> USED: ${totalUsageMillis/1000}s, LIMIT: ${limitMillis/1000}s, REMAIN: ${(limitMillis - totalUsageMillis)/1000}s, Unlocked: $isCurrentlyUnlocked")

        if (totalUsageMillis >= limitMillis && !isCurrentlyUnlocked) {
            Log.d(TAG, "[ACTION] TRIGGERING INSTANT ENFORCEMENT for $packageName")
            
            // PRIORITY 1: Instant System-Level Home Action
            performGlobalAction(GLOBAL_ACTION_HOME)
            
            notificationHelper.showLimitReachedNotification(lockedApp.appName)
            
            val sessionId = activeSessionId
            if (sessionId != null) {
                serviceScope.launch { updateActiveSessionInternal(packageName, sessionId) }
            }
            
            pollingJob?.cancel()
            heartbeatJob?.cancel()
            currentSessionPackage = null
            activeSessionId = null

            withContext(Dispatchers.Main) {
                launchBlockingOverlay(packageName)
            }
            return false
        }
        
        return (limitMillis - totalUsageMillis) < Constants.PRECISION_THRESHOLD_MS
    }

    private suspend fun closeCurrentSessionInternal() {
        val pkg = currentSessionPackage ?: return
        val sessionId = activeSessionId ?: return
        
        pollingJob?.cancel()
        heartbeatJob?.cancel()
        
        updateActiveSessionInternal(pkg, sessionId)
        
        currentSessionPackage = null
        activeSessionId = null
        Log.d(TAG, "[STATE] Closed session for $pkg")
    }

    private fun launchBlockingOverlay(packageName: String) {
        if (lastForegroundPackage == this.packageName) return

        val intent = Intent(this, BlockingActivity::class.java).apply {
            putExtra("locked_package_name", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) // Faster appearance
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            stateMutex.withLock {
                closeCurrentSessionInternal()
            }
            serviceJob.cancel()
        }
    }
}
