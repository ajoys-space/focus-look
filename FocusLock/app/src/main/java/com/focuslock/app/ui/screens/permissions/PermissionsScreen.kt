package com.focuslock.app.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.util.PermissionUtils

/** Represents a single row in the permissions checklist. */
private data class PermissionItem(
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isCritical: Boolean,
    val onRequestClick: () -> Unit
)

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Re-check permission states every time this screen comes back into view —
    // necessary because granting these permissions always happens in a
    // separate system Settings screen, then the user navigates back to us.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissionStates()
        onPauseOrDispose { }
    }

    // Android 13+ requires a runtime dialog specifically for notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationPermissionGranted(granted)
    }

    val items = listOf(
        PermissionItem(
            title = "Usage Access",
            description = "Lets Focus Lock see which app is currently open and track time spent.",
            isGranted = uiState.usageStatsGranted,
            isCritical = true,
            onRequestClick = { PermissionUtils.openUsageStatsSettings(context) }
        ),
        PermissionItem(
            title = "Accessibility Service",
            description = "Lets Focus Lock detect instantly when a locked app is opened.",
            isGranted = uiState.accessibilityGranted,
            isCritical = true,
            onRequestClick = { PermissionUtils.openAccessibilitySettings(context) }
        ),
        PermissionItem(
            title = "Display Over Other Apps",
            description = "Lets Focus Lock show the block screen on top of a locked app.",
            isGranted = uiState.overlayGranted,
            isCritical = true,
            onRequestClick = { PermissionUtils.openOverlaySettings(context) }
        ),
        PermissionItem(
            title = "Notifications",
            description = "Lets Focus Lock alert you when you've reached your limit.",
            isGranted = uiState.notificationsGranted,
            isCritical = false,
            onRequestClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        ),
        PermissionItem(
            title = "Ignore Battery Optimization",
            description = "Prevents Android from killing Focus Lock's tracking in the background.",
            isGranted = uiState.batteryOptimizationIgnored,
            isCritical = false,
            onRequestClick = { PermissionUtils.requestIgnoreBatteryOptimizations(context) }
        ),
        PermissionItem(
            title = "Autostart (Some Devices)",
            description = "On Xiaomi/Oppo/Vivo/Honor phones, enable this so Focus Lock restarts after reboot. Not applicable on all devices — no standard Android API exists for this.",
            isGranted = false, // No reliable way to verify this one's state
            isCritical = false,
            onRequestClick = { PermissionUtils.openAutoStartSettings(context) }
        )
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "Permissions Needed",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Focus Lock needs these permissions to block distracting apps. This is a one-time setup.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                PermissionCard(item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAllPermissionsGranted,
            enabled = uiState.allCriticalPermissionsGranted,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (uiState.allCriticalPermissionsGranted) "Continue" else "Grant Critical Permissions to Continue")
        }
    }
}

@Composable
private fun PermissionCard(item: PermissionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isGranted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (item.isGranted) Color(0xFF00C853) else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.title, fontWeight = FontWeight.SemiBold)
                    if (item.isCritical) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Required",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!item.isGranted) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = item.onRequestClick) {
                    Text("Enable")
                }
            }
        }
    }
}