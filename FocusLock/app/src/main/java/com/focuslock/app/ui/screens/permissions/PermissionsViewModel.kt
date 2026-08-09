package com.focuslock.app.ui.screens.permissions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.service.AppBlockerAccessibilityService
import com.focuslock.app.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Represents the granted/denied state of every special permission we need. */
data class PermissionsUiState(
    val usageStatsGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val allCriticalPermissionsGranted: Boolean = false
)

/**
 * AndroidViewModel (not plain ViewModel) because every permission check here
 * needs a Context — AndroidViewModel gives us Application context safely
 * without leaking an Activity reference.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    application: Application,
    private val repository: FocusLockRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionStates()
        markPermissionsAsSeen()
    }

    private fun markPermissionsAsSeen() {
        viewModelScope.launch {
            repository.setPermissionsSeen(true)
        }
    }

    /**
     * Call this every time the Permissions screen resumes (e.g. after the user
     * comes back from a system Settings screen), since there's no callback API
     * for most of these — we simply re-check on resume.
     */
    fun refreshPermissionStates() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            val usageStats = PermissionUtils.hasUsageStatsPermission(context)
            val accessibility = PermissionUtils.isAccessibilityServiceEnabled(
                context,
                AppBlockerAccessibilityService::class.java
            )
            val overlay = PermissionUtils.hasOverlayPermission(context)
            val batteryIgnored = PermissionUtils.isIgnoringBatteryOptimizations(context)

            // Notification permission checked separately via Compose's runtime
            // permission API in the screen itself (Android 13+ specific flow).
            val notifications = _uiState.value.notificationsGranted

            _uiState.value = PermissionsUiState(
                usageStatsGranted = usageStats,
                accessibilityGranted = accessibility,
                overlayGranted = overlay,
                notificationsGranted = notifications,
                batteryOptimizationIgnored = batteryIgnored,
                allCriticalPermissionsGranted = usageStats && accessibility && overlay
            )
        }
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsGranted = granted)
        refreshPermissionStates()
    }
}