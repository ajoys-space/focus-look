package com.focuslock.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.ui.components.UsageDialGauge
import com.focuslock.app.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAppSelection: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAllAppsUsage: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    // FAB Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    // Staggered entry animation states
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Focus Lock", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToSettings()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAppSelection()
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Manage Apps") },
                modifier = Modifier.scale(fabScale)
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(400))
            },
            label = "homeContentLoading"
        ) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp) // 120.dp for FAB padding
                ) {
                    if (uiState.isCriticalPermissionMissing) {
                        item {
                            PermissionWarningBanner(onAction = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToPermissions()
                            })
                        }
                    }

                    // Usage Ring with Staggered Entry
                    item {
                        AnimatedVisibility(
                            visibleState = visibleState,
                            enter = slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                        ) {
                            UsageRingCard(
                                usageMinutes = uiState.todayUsageMinutes,
                                limitMinutes = uiState.totalDailyLimitMinutes,
                                lockedAppsCount = uiState.lockedAppsCount,
                                unlockedAppsCount = uiState.unlockedAppsCount,
                                streak = uiState.currentStreak
                            )
                        }
                    }

                    // Tracked Apps Section
                    if (uiState.trackedApps.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tracked Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = onNavigateToAllAppsUsage) {
                                    Text("View All Apps")
                                }
                            }
                        }

                        items(uiState.trackedApps.take(3), key = { it.packageName }) { app ->
                            AppUsageItemRow(app)
                        }

                        if (uiState.trackedApps.size > 3) {
                            item {
                                OutlinedButton(
                                    onClick = onNavigateToAllAppsUsage,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("View More Apps")
                                }
                            }
                        }
                    }

                    // Quick Actions with slightly more delay
                    item {
                        AnimatedVisibility(
                            visibleState = visibleState,
                            enter = slideInVertically(spring(stiffness = Spring.StiffnessVeryLow)) { it / 2 } + fadeIn()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.BarChart,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Quick Insights",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onNavigateToStatistics()
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text("View Detailed Statistics")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animates its progress ring in whenever the underlying values change,
 * using animateFloatAsState so the arc smoothly eases toward the new
 * value instead of snapping.
 */
@Composable
private fun UsageRingCard(
    usageMinutes: Int,
    limitMinutes: Int,
    lockedAppsCount: Int,
    unlockedAppsCount: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UsageDialGauge(
                usageMinutes = usageMinutes,
                limitMinutes = limitMinutes,
                modifier = Modifier.size(200.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeStat(label = "Locked", value = lockedAppsCount.toString())
                HomeStat(label = "Unlocked", value = unlockedAppsCount.toString())
                HomeStat(label = "Streak", value = "$streak d")
            }
        }
    }
}

@Composable
private fun AppUsageItemRow(app: AppUsageItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isUnlocked) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 2.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (app.icon != null) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val timeString by remember(app.usageMillis, app.limitMinutes) {
                        derivedStateOf {
                            if (app.limitMinutes < 60) {
                                "${app.usageMillis / 1000} seconds"
                            } else {
                                val hours = app.usageMillis / 3600000
                                val minutes = (app.usageMillis % 3600000) / 60000
                                val seconds = (app.usageMillis % 60000) / 1000
                                if (hours > 0) "${hours}h ${minutes}m ${seconds}s" else "${minutes}m ${seconds}s"
                            }
                        }
                    }
                    
                    Text(
                        if (app.isUnlocked) "Currently Unlocked" else "$timeString / ${app.limitMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (app.isUnlocked) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val targetProgress = (app.usageMillis.toFloat() / (app.limitMinutes * 60000f)).coerceIn(0f, 1f)
            val progress by animateFloatAsState(
                targetValue = targetProgress,
                animationSpec = tween(1000, easing = LinearEasing),
                label = "appUsageProgress"
            )
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.extraLarge),
                color = when {
                    app.isUnlocked -> MaterialTheme.colorScheme.primary
                    progress >= 0.9f -> MaterialTheme.colorScheme.error
                    progress >= 0.7f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun HomeStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PermissionWarningBanner(onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Critical Permissions Missing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Focus Lock cannot block apps or track usage without Accessibility and Usage Access permissions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Grant Permissions")
            }
        }
    }
}
