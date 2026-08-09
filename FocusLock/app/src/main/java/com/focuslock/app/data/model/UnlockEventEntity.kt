package com.focuslock.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records every time the user completed a challenge and earned temporary
 * access back into a locked app. Powers the "unlock count" and "challenge
 * completion count" stats from your spec's Statistics screen.
 */
@Entity(
    tableName = "unlock_events",
    foreignKeys = [
        ForeignKey(
            entity = LockedAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packageName"), Index("dateEpochDay")]
)
data class UnlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val challengeType: String,        // e.g. "MATH", "MEMORY", "TYPING" — matches Step 15's engine
    val unlockedAtMillis: Long,
    val unlockDurationMinutes: Int,
    val dateEpochDay: Long
)