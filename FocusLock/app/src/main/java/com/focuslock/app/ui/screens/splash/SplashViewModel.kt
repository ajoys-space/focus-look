package com.focuslock.app.ui.screens.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.service.AppBlockerAccessibilityService
import com.focuslock.app.ui.navigation.Routes
import com.focuslock.app.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: FocusLockRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _targetRoute = MutableStateFlow<String?>(null)
    val targetRoute: StateFlow<String?> = _targetRoute.asStateFlow()

    init {
        determineNextRoute()
    }

    private fun determineNextRoute() {
        viewModelScope.launch {
            val prefs = repository.userPreferences.first()
            val context = getApplication<Application>().applicationContext

            val nextRoute = when {
                !prefs.onboardingCompleted -> Routes.ONBOARDING
                !prefs.permissionsSeen && !checkCriticalPermissions(context) -> Routes.PERMISSIONS
                else -> Routes.HOME
            }
            _targetRoute.value = nextRoute
        }
    }

    private fun checkCriticalPermissions(context: android.content.Context): Boolean {
        val usageStats = PermissionUtils.hasUsageStatsPermission(context)
        val accessibility = PermissionUtils.isAccessibilityServiceEnabled(
            context,
            AppBlockerAccessibilityService::class.java
        )
        val overlay = PermissionUtils.hasOverlayPermission(context)
        return usageStats && accessibility && overlay
    }
}
