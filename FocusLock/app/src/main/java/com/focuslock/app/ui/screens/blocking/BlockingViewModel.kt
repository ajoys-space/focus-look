package com.focuslock.app.ui.screens.blocking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.model.UnlockEventEntity
import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.ui.screens.challenges.ChallengeRegistry
import com.focuslock.app.ui.screens.challenges.ChallengeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BlockingUiState(
    val packageName: String = "",
    val appName: String = "",
    val dailyLimitMinutes: Int = 0,
    val todayUsageMinutes: Int = 0,
    val unlockDurationMinutes: Int = 5,
    val selectedChallenge: ChallengeType = ChallengeType.QUOTE,
    val isLoading: Boolean = true,
    val isUnlocked: Boolean = false
)

@HiltViewModel
class BlockingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FocusLockRepository
) : ViewModel() {

    private val packageName: String =
        savedStateHandle["locked_package_name"] ?: ""

    private val _uiState = MutableStateFlow(BlockingUiState(packageName = packageName))
    val uiState: StateFlow<BlockingUiState> = _uiState.asStateFlow()

    init {
        if (packageName.isNotEmpty()) {
            loadData()
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val app = repository.getLockedApp(packageName)
            val usageMillis = repository.getTodayUsageMillis(packageName)

            _uiState.value = _uiState.value.copy(
                appName = app?.appName ?: packageName,
                dailyLimitMinutes = app?.dailyLimitMinutes ?: 0,
                unlockDurationMinutes = app?.unlockDurationMinutes ?: 5,
                todayUsageMinutes = (usageMillis / 60000).toInt(),
                selectedChallenge = ChallengeRegistry.pickRandomChallenge(),
                isLoading = false
            )
        }
    }

    fun onChallengeCompleted() {
        viewModelScope.launch {
            val app = repository.getLockedApp(packageName) ?: return@launch

            repository.recordUnlockEvent(
                UnlockEventEntity(
                    packageName = packageName,
                    challengeType = _uiState.value.selectedChallenge.name,
                    unlockedAtMillis = System.currentTimeMillis(),
                    unlockDurationMinutes = app.unlockDurationMinutes,
                    dateEpochDay = LocalDate.now().toEpochDay()
                )
            )

            _uiState.value = _uiState.value.copy(isUnlocked = true)
        }
    }
}