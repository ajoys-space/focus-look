package com.focuslock.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focuslock.app.data.model.DailyStreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(streak: DailyStreakEntity)

    @Query("SELECT * FROM daily_streak WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getForDay(dateEpochDay: Long): DailyStreakEntity?

    // Ordered descending from today backwards — Repository walks this list
    // to count consecutive successful days for the streak counter.
    @Query("SELECT * FROM daily_streak ORDER BY dateEpochDay DESC")
    fun getAllStreakDays(): Flow<List<DailyStreakEntity>>
}