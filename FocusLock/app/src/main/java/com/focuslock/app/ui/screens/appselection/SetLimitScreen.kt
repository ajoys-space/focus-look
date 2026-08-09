package com.focuslock.app.ui.screens.appselection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLimitScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SetLimitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.appName.isNotBlank()) uiState.appName else "Set Limit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = uiState.dailyLimitMinutes > 0,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
            ) {
                Text("Save Limit")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize()) {
            Text("Daily Limit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Constants.DEFAULT_LIMIT_OPTIONS_MINUTES.forEach { minutes ->
                LimitOptionRow(
                    label = formatMinutes(minutes),
                    isSelected = !uiState.useCustomLimit && uiState.dailyLimitMinutes == minutes,
                    onClick = { viewModel.selectPresetLimit(minutes) }
                )
            }

            LimitOptionRow(
                label = "Custom",
                isSelected = uiState.useCustomLimit,
                onClick = { viewModel.enableCustomLimit() }
            )

            if (uiState.useCustomLimit) {
                OutlinedTextField(
                    value = uiState.customLimitInput,
                    onValueChange = viewModel::onCustomLimitChanged,
                    label = { Text("Minutes") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Unlock Duration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "How long the app stays unlocked after completing a challenge",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Constants.DEFAULT_UNLOCK_DURATIONS_MINUTES.forEach { minutes ->
                    FilterChip(
                        selected = uiState.unlockDurationMinutes == minutes,
                        onClick = { viewModel.selectUnlockDuration(minutes) },
                        label = { Text("$minutes min") }
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

private fun formatMinutes(minutes: Int): String = when {
    minutes < 60 -> "$minutes minutes"
    minutes == 60 -> "1 hour"
    else -> "${minutes / 60} hours"
}