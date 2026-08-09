package com.focuslock.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom circular dial gauge that displays app usage versus a daily limit.
 * 
 * @param usageMinutes The number of minutes used so far.
 * @param limitMinutes The daily limit in minutes.
 * @param modifier Modifier for sizing and layout.
 */
@Composable
fun UsageDialGauge(
    usageMinutes: Int,
    limitMinutes: Int,
    modifier: Modifier = Modifier
) {
    // 1. Calculate percentage and target color
    val effectiveLimit = limitMinutes.coerceAtLeast(1)
    val percentage = (usageMinutes.toFloat() / effectiveLimit).coerceIn(0f, 1.2f) // Allow slight overflow visual
    
    // Smoothly interpolate color based on usage percentage
    val targetColor = when {
        percentage < 0.5f -> lerp(Color(0xFF4CAF50), Color(0xFFFFC107), percentage * 2) // Green to Yellow
        percentage < 0.85f -> lerp(Color(0xFFFFC107), Color(0xFFF44336), (percentage - 0.5f) / 0.35f) // Yellow to Red
        else -> Color(0xFFF44336) // Red
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "dialColor"
    )

    // 2. Animate the sweep progress
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialProgress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val canvasSize = size.minDimension - strokeWidth
            
            // Background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset((size.width - canvasSize) / 2, (size.height - canvasSize) / 2),
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active sweep arc
            // We start from -90 degrees (top) and sweep clockwise
            drawArc(
                color = animatedColor,
                startAngle = -90f,
                sweepAngle = (animatedProgress * 360f).coerceIn(0.1f, 360f),
                useCenter = false,
                topLeft = Offset((size.width - canvasSize) / 2, (size.height - canvasSize) / 2),
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = usageMinutes.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 1.sp
            )
            Text(
                text = "of $limitMinutes min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
