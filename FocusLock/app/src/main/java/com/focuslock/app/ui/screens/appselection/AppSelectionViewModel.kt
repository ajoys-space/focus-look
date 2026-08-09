package com.focuslock.app.ui.screens.appselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.local.InstalledApp
import com.focuslock.app.data.local.InstalledAppsHelper
import com.focuslock.app.data.model.LockedAppEntity
import com.focuslock.app.data.repository.FocusLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSelectionUiState(
    val allApps: List<InstalledApp> = emptyList(),
    val alreadyLockedPackages: Set<String> = emptySet(),
    val selectedPackages: Set<String> = emptySet(),
    val pendingAppForLimit: InstalledApp? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val filteredApps: List<InstalledApp>
        get() = if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
}

/**
 * Search input is debounced 250ms before being applied to filteredApps —
 * on a device with a large app list, filtering on every single keystroke
 * (especially combined with recomposition) is wasted CPU work for
 * intermediate states the user never actually pauses on.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val installedAppsHelper: InstalledAppsHelper,
    private val repository: FocusLockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    private val rawSearchInput = MutableSharedFlow<String>(replay = 1)

    init {
        loadApps()
        viewModelScope.launch {
            rawSearchInput
                .debounce(250)
                .collect { query ->
                    _uiState.value = _uiState.value.copy(searchQuery = query)
                }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = installedAppsHelper.getLaunchableApps()
            val lockedPackages = repository.getAllLockedApps().first().map { it.packageName }.toSet()

            _uiState.value = _uiState.value.copy(
                allApps = apps,
                alreadyLockedPackages = lockedPackages,
                selectedPackages = lockedPackages,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch { rawSearchInput.emit(query) }
    }

    fun toggleAppSelected(packageName: String) {
        val current = _uiState.value.selectedPackages
        if (packageName in current) {
            // Unselecting: just remove from the temporary set
            _uiState.value = _uiState.value.copy(
                selectedPackages = current - packageName
            )
        } else {
            // Selecting: show the limit dialog first
            val app = _uiState.value.allApps.find { it.packageName == packageName }
            _uiState.value = _uiState.value.copy(pendingAppForLimit = app)
        }
    }

    fun setLimitForPendingApp(minutes: Int) {
        val app = _uiState.value.pendingAppForLimit ?: return
        viewModelScope.launch {
            repository.addOrUpdateLockedApp(
                LockedAppEntity(
                    packageName = app.packageName,
                    appName = app.appName,
                    dailyLimitMinutes = minutes
                )
            )
            val current = _uiState.value.selectedPackages
            _uiState.value = _uiState.value.copy(
                selectedPackages = current + app.packageName,
                alreadyLockedPackages = _uiState.value.alreadyLockedPackages + app.packageName,
                pendingAppForLimit = null
            )
        }
    }

    fun cancelPendingLimit() {
        _uiState.value = _uiState.value.copy(pendingAppForLimit = null)
    }

    fun saveSelection(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            // Since we now save on every limit selection, "Save Changes" 
            // primarily handles removals in this new flow.
            val toRemove = state.alreadyLockedPackages - state.selectedPackages

            toRemove.forEach { packageName ->
                repository.removeLockedApp(packageName)
            }

            onComplete()
        }
    }
}
