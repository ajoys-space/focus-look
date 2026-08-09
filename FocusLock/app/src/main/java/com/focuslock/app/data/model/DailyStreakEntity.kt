package com.focuslock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per calendar day, tracking whether the user stayed within all
 * their limits that day. Streak count is computed by the Repository
 * (consecutive successDays), not stored as a running number here — this
 * keeps the source data honest even if the user reinstalls or the app
 * needs to recompute history.
 */
@Entity(tableName = "daily_streak")
data class DailyStreakEntity(
    @PrimaryKey val dateEpochDay: Long,
    val staysWithinLimits: Boolean,
    val totalUsageMinutes: Int
)