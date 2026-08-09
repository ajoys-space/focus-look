package com.focuslock.app.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.local.BackupHelper
import com.focuslock.app.data.model.ThemeMode
import com.focuslock.app.data.model.UserPreferences
import com.focuslock.app.data.repository.FocusLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val backupMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FocusLockRepository,
    private val backupHelper: BackupHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferences.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val result = backupHelper.exportToUri(uri)
            _uiState.value = _uiState.value.copy(
                backupMessage = if (result.isSuccess) "Backup exported successfully"
                else "Export failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            val result = backupHelper.importFromUri(uri)
            _uiState.value = _uiState.value.copy(
                backupMessage = if (result.isSuccess) "Restored ${result.getOrNull()} apps"
                else "Restore failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearBackupMessage() {
        _uiState.value = _uiState.value.copy(backupMessage = null)
    }
}