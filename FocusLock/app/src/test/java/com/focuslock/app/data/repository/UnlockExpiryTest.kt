package com.focuslock.app.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Mirrors FocusLockRepositoryImpl.getActiveUnlockExpiryMillis()'s core math,
 * tested in isolation from Room/DataStore.
 */
private fun isUnlockActive(unlockedAtMillis: Long, durationMinutes: Int, now: Long): Boolean {
    val expiresAt = unlockedAtMillis + (durationMinutes * 60_000L)
    return now < expiresAt
}

class UnlockExpiryTest {

    @Test
    fun `unlock is active immediately after unlocking`() {
        val unlockedAt = 1_000_000L
        val now = unlockedAt + 1000 // 1 second later
        assertThat(isUnlockActive(unlockedAt, 5, now)).isTrue()
    }

    @Test
    fun `unlock expires exactly at duration boundary`() {
        val unlockedAt = 1_000_000L
        val fiveMinutesLater = unlockedAt + (5 * 60_000L)
        assertThat(isUnlockActive(unlockedAt, 5, fiveMinutesLater)).isFalse()
    }

    @Test
    fun `unlock is still active one second before expiry`() {
        val unlockedAt = 1_000_000L
        val justBeforeExpiry = unlockedAt + (5 * 60_000L) - 1000
        assertThat(isUnlockActive(unlockedAt, 5, justBeforeExpiry)).isTrue()
    }

    @Test
    fun `unlock is expired well after duration`() {
        val unlockedAt = 1_000_000L
        val muchLater = unlockedAt + (60 * 60_000L) // 1 hour later
        assertThat(isUnlockActive(unlockedAt, 5, muchLater)).isFalse()
    }
}