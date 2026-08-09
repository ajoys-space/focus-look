package com.focuslock.app.ui.screens.appselection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.repository.FocusLockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetLimitUiState(
    val packageName: String = "",
    val appName: String = "",
    val dailyLimitMinutes: Int = 60,
    val unlockDurationMinutes: Int = 5,
    val useCustomLimit: Boolean = false,
    val customLimitInput: String = "",
    val isLoading: Boolean = true
)

/**
 * SavedStateHandle lets us receive the packageName navigation argument
 * directly, Hilt-injected — the standard pattern for passing arguments
 * into a Hilt ViewModel via Navigation Compose.
 */
@HiltViewModel
class SetLimitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FocusLockRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(SetLimitUiState())
    val uiState: StateFlow<SetLimitUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val app = repository.getLockedApp(packageName)
            if (app != null) {
                _uiState.value = SetLimitUiState(
                    packageName = app.packageName,
                    appName = app.appName,
                    dailyLimitMinutes = app.dailyLimitMinutes,
                    unlockDurationMinutes = app.unlockDurationMinutes,
                    useCustomLimit = app.dailyLimitMinutes !in listOf(30, 60, 120),
                    customLimitInput = app.dailyLimitMinutes.toString(),
                    isLoading = false
                )
            }
        }
    }

    fun selectPresetLimit(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            dailyLimitMinutes = minutes,
            useCustomLimit = false
        )
    }

    fun enableCustomLimit() {
        _uiState.value = _uiState.value.copy(useCustomLimit = true)
    }

    fun onCustomLimitChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }
        val minutes = digitsOnly.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            customLimitInput = digitsOnly,
            dailyLimitMinutes = minutes
        )
    }

    fun selectUnlockDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(unlockDurationMinutes = minutes)
    }

    fun save(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.dailyLimitMinutes <= 0) return@launch // Guard against saving an invalid 0-minute limit

            val existing = repository.getLockedApp(packageName) ?: return@launch
            repository.addOrUpdateLockedApp(
                existing.copy(
                    dailyLimitMinutes = state.dailyLimitMinutes,
                    unlockDurationMinutes = state.unlockDurationMinutes
                )
            )
            onComplete()
        }
    }
}