package com.focuslock.app.ui.screens.challenges

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChallengeScreen(
    challengeType: ChallengeType,
    onCompleted: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when (challengeType) {
            ChallengeType.QUOTE -> QuoteChallengeScreen(onCompleted = onCompleted)
            ChallengeType.MATH -> MathChallengeScreen(onCompleted = onCompleted)
            ChallengeType.MEMORY -> MemoryChallengeScreen(onCompleted = onCompleted)
            ChallengeType.TYPING -> TypingChallengeScreen(onCompleted = onCompleted)
            ChallengeType.WALKING -> WalkingChallengeScreen(onCompleted = onCompleted)
            ChallengeType.BREATHING -> QuoteChallengeScreen(onCompleted = onCompleted) // placeholder — breathing exercise not in your 28-step list as its own step; can add later if wanted
        }
    }
}