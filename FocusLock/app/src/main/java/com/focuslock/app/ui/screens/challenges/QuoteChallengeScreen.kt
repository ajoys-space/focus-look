package com.focuslock.app.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Original lines written for this app — not attributed to any external author. */
private val focusQuotes = listOf(
    "The time you're about to spend here is time you're choosing back for yourself.",
    "A short pause now can save you an hour of scrolling later.",
    "You don't need this app right now. You need five minutes of quiet.",
    "Every time you wait it out, the habit gets a little weaker.",
    "Boredom is not an emergency. It's just a feeling passing through."
)

/**
 * Challenge F from the spec: read a motivational message and stay on
 * screen for 60 seconds before continuing is allowed.
 */
@Composable
fun QuoteChallengeScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quote = remember { focusQuotes.random() }
    var secondsRemaining by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u201C$quote\u201D",
            style = MaterialTheme.typography.headlineLarge,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (secondsRemaining > 0) {
            CircularProgressIndicator(progress = { (60 - secondsRemaining) / 60f })
            Spacer(modifier = Modifier.height(16.dp))
            Text("$secondsRemaining seconds remaining")
        } else {
            Button(
                onClick = onCompleted,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Continue")
            }
        }
    }
}