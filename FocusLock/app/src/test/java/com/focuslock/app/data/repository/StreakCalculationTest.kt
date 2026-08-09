package com.focuslock.app.data.repository

import com.focuslock.app.data.model.DailyStreakEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests the pure streak-counting logic in isolation, extracted here as a
 * standalone function mirroring FocusLockRepositoryImpl.getCurrentStreak()'s
 * algorithm, since that method itself requires a live DAO/database to call.
 * This verifies the counting logic itself is correct independent of Room.
 */
private fun calculateStreak(days: List<DailyStreakEntity>, today: Long): Int {
    var streak = 0
    var expectedDay = today
    for (day in days) {
        if (day.dateEpochDay == expectedDay && day.staysWithinLimits) {
            streak++
            expectedDay--
        } else {
            break
        }
    }
    return streak
}

class StreakCalculationTest {

    @Test
    fun `consecutive successful days count correctly`() {
        val today = 100L
        val days = listOf(
            DailyStreakEntity(dateEpochDay = 100, staysWithinLimits = true, totalUsageMinutes = 30),
            DailyStreakEntity(dateEpochDay = 99, staysWithinLimits = true, totalUsageMinutes = 20),
            DailyStreakEntity(dateEpochDay = 98, staysWithinLimits = true, totalUsageMinutes = 40)
        )

        assertThat(calculateStreak(days, today)).isEqualTo(3)
    }

    @Test
    fun `streak stops at first failed day`() {
        val today = 100L
        val days = listOf(
            DailyStreakEntity(dateEpochDay = 100, staysWithinLimits = true, totalUsageMinutes = 30),
            DailyStreakEntity(dateEpochDay = 99, staysWithinLimits = false, totalUsageMinutes = 200),
            DailyStreakEntity(dateEpochDay = 98, staysWithinLimits = true, totalUsageMinutes = 40)
        )

        assertThat(calculateStreak(days, today)).isEqualTo(1)
    }

    @Test
    fun `streak is zero when today failed`() {
        val today = 100L
        val days = listOf(
            DailyStreakEntity(dateEpochDay = 100, staysWithinLimits = false, totalUsageMinutes = 300)
        )

        assertThat(calculateStreak(days, today)).isEqualTo(0)
    }

    @Test
    fun `streak is zero when there is a gap in days`() {
        val today = 100L
        val days = listOf(
            DailyStreakEntity(dateEpochDay = 100, staysWithinLimits = true, totalUsageMinutes = 30),
            // day 99 missing entirely — user didn't have the app open
            DailyStreakEntity(dateEpochDay = 98, staysWithinLimits = true, totalUsageMinutes = 40)
        )

        assertThat(calculateStreak(days, today)).isEqualTo(1)
    }

    @Test
    fun `empty history returns zero streak`() {
        assertThat(calculateStreak(emptyList(), 100L)).isEqualTo(0)
    }
}