package com.focuslock.app.ui.screens.challenges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/** One generated math problem and its correct answer. */
private data class MathProblem(
    val question: String,
    val answer: Int
)

private fun generateProblem(): MathProblem {
    val operation = listOf("+", "-", "×", "÷").random()
    return when (operation) {
        "+" -> {
            val a = Random.nextInt(1, 50)
            val b = Random.nextInt(1, 50)
            MathProblem("$a + $b", a + b)
        }
        "-" -> {
            val a = Random.nextInt(10, 99)
            val b = Random.nextInt(1, a) // ensures a non-negative result
            MathProblem("$a − $b", a - b)
        }
        "×" -> {
            val a = Random.nextInt(2, 12)
            val b = Random.nextInt(2, 12)
            MathProblem("$a × $b", a * b)
        }
        else -> { // "÷" — constructed so it always divides evenly
            val b = Random.nextInt(2, 12)
            val answer = Random.nextInt(2, 12)
            val a = b * answer
            MathProblem("$a ÷ $b", answer)
        }
    }
}

private const val TOTAL_QUESTIONS = 10

/**
 * Challenge A from the spec: 10 random math questions, mixed operations.
 * All 10 must be answered correctly in sequence before completion — a
 * wrong answer regenerates a new problem in the same slot rather than
 * ending the challenge, so it stays a genuine attention task rather than
 * a one-shot gate.
 */
@Composable
fun MathChallengeScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionIndex by remember { mutableStateOf(0) }
    var currentProblem by remember { mutableStateOf(generateProblem()) }
    var input by remember { mutableStateOf("") }
    var showWrongFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Question ${questionIndex + 1} of $TOTAL_QUESTIONS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { questionIndex / TOTAL_QUESTIONS.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = currentProblem.question,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it.filter { c -> c.isDigit() || c == '-' }
                showWrongFeedback = false
            },
            label = { Text("Your answer") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showWrongFeedback,
            supportingText = {
                if (showWrongFeedback) Text("Not quite — try the new problem")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val answer = input.toIntOrNull()
                if (answer == currentProblem.answer) {
                    input = ""
                    if (questionIndex + 1 >= TOTAL_QUESTIONS) {
                        onCompleted()
                    } else {
                        questionIndex++
                        currentProblem = generateProblem()
                    }
                } else {
                    showWrongFeedback = true
                    currentProblem = generateProblem()
                    input = ""
                }
            },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Submit")
        }
    }
}