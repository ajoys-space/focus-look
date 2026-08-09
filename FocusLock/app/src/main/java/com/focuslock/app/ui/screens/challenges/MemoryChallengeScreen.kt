package com.focuslock.app.ui.screens.challenges

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val tileColors = listOf(
    Color(0xFFE53935), // red
    Color(0xFF1E88E5), // blue
    Color(0xFF43A047), // green
    Color(0xFFFDD835)  // yellow
)

private const val ROUNDS_TO_WIN = 5      // Sequence length needed to pass
private const val STARTING_LENGTH = 3    // Initial sequence length

private enum class GamePhase { SHOWING, INPUT, WRONG }

/**
 * Challenge B from the spec: repeat an increasingly long tile pattern.
 * Sequence grows by 1 each successful round; reaching ROUNDS_TO_WIN
 * (sequence length STARTING_LENGTH + ROUNDS_TO_WIN - 1) completes the
 * challenge. A wrong tap restarts the current round's sequence (not the
 * whole challenge from zero), keeping frustration reasonable.
 */
@Composable
fun MemoryChallengeScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sequence by remember { mutableStateOf(generateSequence(STARTING_LENGTH)) }
    var round by remember { mutableStateOf(1) }
    var phase by remember { mutableStateOf(GamePhase.SHOWING) }
    var showingIndex by remember { mutableStateOf(-1) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }

    LaunchedEffect(sequence) {
        phase = GamePhase.SHOWING
        playerInput = emptyList()
        for (i in sequence.indices) {
            showingIndex = sequence[i]
            delay(500)
            showingIndex = -1
            delay(200)
        }
        phase = GamePhase.INPUT
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Round $round of $ROUNDS_TO_WIN",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (phase) {
                GamePhase.SHOWING -> "Watch the pattern..."
                GamePhase.INPUT -> "Repeat it back"
                GamePhase.WRONG -> "Not quite — watch again"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        TileGrid(
            activeIndex = showingIndex,
            enabled = phase == GamePhase.INPUT,
            onTileTap = { tileIndex ->
                val expectedIndex = playerInput.size
                if (sequence[expectedIndex] == tileIndex) {
                    val newInput = playerInput + tileIndex
                    playerInput = newInput
                    if (newInput.size == sequence.size) {
                        if (round >= ROUNDS_TO_WIN) {
                            onCompleted()
                        } else {
                            round++
                            sequence = generateSequence(sequence.size + 1)
                        }
                    }
                } else {
                    phase = GamePhase.WRONG
                }
            }
        )

        if (phase == GamePhase.WRONG) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { sequence = generateSequence(sequence.size) }) {
                Text("Try This Round Again")
            }
        }
    }
}

private fun generateSequence(length: Int): List<Int> =
    List(length) { Random.nextInt(tileColors.size) }

@Composable
private fun TileGrid(
    activeIndex: Int,
    enabled: Boolean,
    onTileTap: (Int) -> Unit
) {
    Column {
        for (row in 0..1) {
            Row {
                for (col in 0..1) {
                    val index = row * 2 + col
                    MemoryTile(
                        color = tileColors[index],
                        isActive = activeIndex == index,
                        enabled = enabled,
                        onClick = { onTileTap(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryTile(
    color: Color,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val displayColor by animateColorAsState(
        targetValue = if (isActive) color else color.copy(alpha = 0.35f),
        animationSpec = tween(150),
        label = "tileColor"
    )

    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(90.dp)
            .background(displayColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
    )
}