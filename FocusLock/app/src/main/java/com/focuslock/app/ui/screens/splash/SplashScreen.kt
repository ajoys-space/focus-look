package com.focuslock.app.ui.screens.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

/**
 * Splash screen — its real job is deciding where to route next; the
 * branding animation here is a spring-based scale + fade-in, deliberately
 * brief and subtle rather than flashy, matching the app's calm-focus tone.
 */
@Composable
fun SplashScreen(
    onNavigateNext: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var animateIn by remember { mutableStateOf(false) }
    val targetRoute by viewModel.targetRoute.collectAsState()

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "splashAlpha"
    )

    LaunchedEffect(targetRoute) {
        animateIn = true
        if (targetRoute != null) {
            delay(1200)
            onNavigateNext(targetRoute!!)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Focus Lock",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        )
    }
}