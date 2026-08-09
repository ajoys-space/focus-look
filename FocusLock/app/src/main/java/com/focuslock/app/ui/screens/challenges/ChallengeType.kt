package com.focuslock.app.ui.screens.challenges

/**
 * Every challenge type from the spec. Steps 16-19 build the real UI for
 * MATH, MEMORY, TYPING, and WALKING. Only types listed in
 * ChallengeRegistry.implementedTypes are actually selectable right now.
 */
enum class ChallengeType(val displayName: String) {
    MATH("Math Problems"),
    MEMORY("Memory Game"),
    TYPING("Typing Challenge"),
    BREATHING("Breathing Exercise"),
    WALKING("Walking Challenge"),
    QUOTE("Motivational Quote")
}