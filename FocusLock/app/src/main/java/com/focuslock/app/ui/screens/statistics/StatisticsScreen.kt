package com.focuslock.app.ui.screens.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportStatsAsPdf() }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsRange.entries.forEachIndexed { index, range ->
                    SegmentedButton(
                        selected = uiState.selectedRange == range,
                        onClick = { viewModel.selectRange(range) },
                        shape = SegmentedButtonDefaults.itemShape(index, StatsRange.entries.size)
                    ) {
                        Text(range.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Screen Time Usage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                UsageBarChart(
                    data = uiState.dailyBreakdown,
                    range = uiState.selectedRange,
                    modifier = Modifier.fillMaxWidth().height(260.dp).padding(top = 20.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Key Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Unlocks Today",
                    value = uiState.totalUnlockCount.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Minutes Saved",
                    value = uiState.timeSavedMinutes.toString()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UsageBarChart(
    data: List<DayUsage>,
    range: StatsRange,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val maxUsage = (data.maxOfOrNull { it.usageMinutes } ?: 0).coerceAtLeast(1)
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = labelColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f, 
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val labelHeight = 24.dp.toPx()
            val chartHeight = size.height - labelHeight
            val barSpacing = size.width / data.size
            val barWidth = (barSpacing * 0.6f).coerceAtMost(32.dp.toPx())

            // 1. Draw Background Grid Lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = (chartHeight / gridLines) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            data.forEachIndexed { index, day ->
                val barHeightRatio = day.usageMinutes.toFloat() / maxUsage.toFloat()
                val targetHeight = chartHeight * barHeightRatio
                val currentHeight = targetHeight * animationProgress.value
                val xOffset = index * barSpacing + (barSpacing - barWidth) / 2

                // 2. Draw Bar
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(xOffset, chartHeight - currentHeight),
                    size = Size(barWidth, currentHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // 3. Draw Label with smart alignment and skipping logic
                val shouldDrawLabel = when (range) {
                    StatsRange.DAILY, StatsRange.WEEKLY -> true
                    StatsRange.MONTHLY -> index % 6 == 0 || index == data.lastIndex
                }

                if (shouldDrawLabel) {
                    val textLayoutResult = textMeasurer.measure(day.label, style = labelStyle)
                    val textWidth = textLayoutResult.size.width
                    val textX = index * barSpacing + (barSpacing - textWidth) / 2
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(textX, chartHeight + 4.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
