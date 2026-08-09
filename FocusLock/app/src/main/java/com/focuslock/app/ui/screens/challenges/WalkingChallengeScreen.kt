package com.focuslock.app.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.local.StepCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TARGET_STEPS = 300

@HiltViewModel
class WalkingChallengeViewModel @Inject constructor(
    private val stepCounterManager: StepCounterManager
) : ViewModel() {

    private val _stepsTaken = MutableStateFlow(0)
    val stepsTaken: StateFlow<Int> = _stepsTaken.asStateFlow()

    val hasSensor: Boolean = stepCounterManager.hasStepSensor()

    init {
        if (hasSensor) {
            viewModelScope.launch {
                stepCounterManager.stepsSinceStart().collect { steps ->
                    _stepsTaken.value = steps
                }
            }
        }
    }
}

/**
 * Challenge E from the spec: walk 300 steps, tracked via the device's
 * hardware step counter. Falls back to an honest "not available on this
 * device" message rather than faking progress when no sensor exists —
 * per the spec's instruction to never claim impossible features.
 */
@Composable
fun WalkingChallengeScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalkingChallengeViewModel = hiltViewModel()
) {
    val steps by viewModel.stepsTaken.collectAsState()

    LaunchedEffect(steps) {
        if (steps >= TARGET_STEPS) onCompleted()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!viewModel.hasSensor) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Step counter not available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This device doesn't have a step-counting sensor, so this challenge can't be completed here. Try a different challenge type.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.Filled.DirectionsWalk,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Walk $TARGET_STEPS steps",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                progress = { (steps.toFloat() / TARGET_STEPS).coerceIn(0f, 1f) },
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$steps / $TARGET_STEPS steps",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}