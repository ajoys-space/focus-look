package com.focuslock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one app the user has chosen to limit.
 * packageName is the primary key since Android package names are guaranteed
 * unique per installed app — no need for a separate auto-increment ID.
 */
@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val unlockDurationMinutes: Int = 5,     // How long an unlock lasts once earned
    val isEnabled: Boolean = true,          // User can pause tracking without deleting the entry
    val createdAtMillis: Long = System.currentTimeMillis()
)