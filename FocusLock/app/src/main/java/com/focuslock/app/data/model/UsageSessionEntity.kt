package com.focuslock.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single continuous stretch of time an app spent in the foreground.
 * We store raw sessions (not just a running total) so we can later build
 * accurate daily/weekly/monthly graphs and know exactly when usage happened,
 * not just how much.
 */
@Entity(
    tableName = "usage_sessions",
    foreignKeys = [
        ForeignKey(
            entity = LockedAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE // Delete usage history if the app is un-tracked
        )
    ],
    indices = [
        Index("packageName"),
        Index("dateEpochDay"),
        Index(value = ["packageName", "startTimeMillis"], unique = true)
    ]
)
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
    val dateEpochDay: Long   // LocalDate.toEpochDay() — lets us group by calendar day efficiently
)