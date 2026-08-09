package com.focuslock.app.ui.screens.blocking

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.ui.screens.challenges.ChallengeScreen

@Composable
fun BlockingScreen(
    onFinish: () -> Unit,
    viewModel: BlockingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showChallenge by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when {
        uiState.isUnlocked -> UnlockedConfirmation(
            appName = uiState.appName,
            unlockDurationMinutes = uiState.unlockDurationMinutes,
            onContinue = onFinish
        )
        showChallenge -> ChallengeScreen(
            challengeType = uiState.selectedChallenge,
            onCompleted = { viewModel.onChallengeCompleted() }
        )
        else -> BlockedContent(
            appName = uiState.appName,
            dailyLimitMinutes = uiState.dailyLimitMinutes,
            todayUsageMinutes = uiState.todayUsageMinutes,
            onUnlockClick = { showChallenge = true }
        )
    }
}

@Composable
private fun BlockedContent(
    appName: String,
    dailyLimitMinutes: Int,
    todayUsageMinutes: Int,
    onUnlockClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "$appName is locked",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You've used $todayUsageMinutes of $dailyLimitMinutes minutes today.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onUnlockClick,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Complete a Challenge to Unlock")
        }
    }
}

/**
 * Celebratory spring-based scale-in on the checkmark — a small, deliberate
 * moment of positive feedback for completing a challenge, matching spec
 * item 22's "modern animations" ask without being excessive.
 */
@Composable
private fun UnlockedConfirmation(
    appName: String,
    unlockDurationMinutes: Int,
    onContinue: () -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "unlockIconScale"
    )

    LaunchedEffect(Unit) { animateIn = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp).scale(iconScale),
            tint = Color(0xFF00C853)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "$appName unlocked",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "You have $unlockDurationMinutes minutes before it locks again.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue")
        }
    }
}