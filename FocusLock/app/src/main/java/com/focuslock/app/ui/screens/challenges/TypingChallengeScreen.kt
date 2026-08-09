package com.focuslock.app.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/** Original passages written for this app — not sourced from any external work. */
private val typingPassages = listOf(
    "Focus is a skill you practice, not a trait you either have or don't. Every time you choose to wait, you get a little better at waiting.",
    "The urge to check your phone will pass whether you check it or not. Most urges last less than two minutes if you simply let them.",
    "Small interruptions add up to lost afternoons. Protecting a few minutes now protects the rest of your day.",
    "You opened this app for a reason. Whatever it was can probably wait until you've finished what you actually meant to do."
)

/**
 * Challenge C from the spec: type a paragraph without mistakes.
 * Live-highlights correct (green), incorrect (red), and untyped (default)
 * characters as the user types. Completion requires the input to exactly
 * match the target passage, including punctuation and spacing.
 */
@Composable
fun TypingChallengeScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetText = remember { typingPassages.random() }
    var input by remember { mutableStateOf("") }

    val isComplete = input == targetText
    val hasError = targetText.zip(input).any { (target, typed) -> target != typed } ||
            input.length > targetText.length

    LaunchedEffect(isComplete) {
        if (isComplete) onCompleted()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Type the passage exactly",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildHighlightedText(targetText, input),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { newValue ->
                // Prevent typing past the target length so error state
                // stays meaningful rather than scrolling indefinitely.
                if (newValue.length <= targetText.length) {
                    input = newValue
                }
            },
            label = { Text(if (hasError) "Check for a typo" else "Start typing") },
            isError = hasError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${input.length} / ${targetText.length} characters",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Builds an AnnotatedString coloring each character of the target passage:
 * green if correctly typed so far, red if the corresponding typed character
 * is wrong, default color for not-yet-reached characters.
 */
private fun buildHighlightedText(target: String, typed: String): AnnotatedString =
    buildAnnotatedString {
        target.forEachIndexed { index, targetChar ->
            val style = when {
                index >= typed.length -> SpanStyle()
                typed[index] == targetChar -> SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF00C853))
                else -> SpanStyle(
                    color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
            withStyle(style) { append(targetChar) }
        }
    }