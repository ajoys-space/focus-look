package com.focuslock.app.ui.screens.challenges

object ChallengeRegistry {

    val implementedTypes: List<ChallengeType> = listOf(
        ChallengeType.QUOTE,
        ChallengeType.MATH,
        ChallengeType.MEMORY,
        ChallengeType.TYPING,
        ChallengeType.WALKING
    )

    fun pickRandomChallenge(): ChallengeType = implementedTypes.random()
}