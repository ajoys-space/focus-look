package com.focuslock.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// import androidx.room.TypeConverters
import com.focuslock.app.data.local.dao.LockedAppDao
import com.focuslock.app.data.local.dao.StreakDao
import com.focuslock.app.data.local.dao.UnlockEventDao
import com.focuslock.app.data.local.dao.UsageSessionDao
import com.focuslock.app.data.model.DailyStreakEntity
import com.focuslock.app.data.model.LockedAppEntity
import com.focuslock.app.data.model.UnlockEventEntity
import com.focuslock.app.data.model.UsageSessionEntity

/**
 * Still version 1 — no schema has changed since Step 6, so no real
 * Migration objects exist yet. What matters for release is that
 * DatabaseModule.kt (below) no longer uses fallbackToDestructiveMigration(),
 * so the FIRST real schema change from here forward is forced to ship
 * with a proper Migration, protecting real users' data on their first
 * update after install.
 */
@Database(
    entities = [
        LockedAppEntity::class,
        UsageSessionEntity::class,
        UnlockEventEntity::class,
        DailyStreakEntity::class
    ],
    version = 2,
    exportSchema = true
)
// @TypeConverters(Converters::class)
abstract class FocusLockDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun unlockEventDao(): UnlockEventDao
    abstract fun streakDao(): StreakDao
}