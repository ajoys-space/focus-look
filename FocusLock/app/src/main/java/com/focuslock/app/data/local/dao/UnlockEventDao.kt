package com.focuslock.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.focuslock.app.data.model.UnlockEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockEventDao {

    @Insert
    suspend fun insert(event: UnlockEventEntity)

    @Query("SELECT * FROM unlock_events WHERE packageName = :packageName ORDER BY unlockedAtMillis DESC")
    fun getEventsForApp(packageName: String): Flow<List<UnlockEventEntity>>

    // Used to check whether an app is CURRENTLY within an active unlock window
    @Query("""
        SELECT * FROM unlock_events
        WHERE packageName = :packageName
        ORDER BY unlockedAtMillis DESC
        LIMIT 1
    """)
    suspend fun getMostRecentUnlockForApp(packageName: String): UnlockEventEntity?

    @Query("SELECT * FROM unlock_events WHERE unlockedAtMillis >= :sinceMillis")
    fun getRecentUnlocks(sinceMillis: Long): Flow<List<UnlockEventEntity>>

    @Query("SELECT COUNT(*) FROM unlock_events WHERE dateEpochDay = :dateEpochDay")
    fun getUnlockCountForDay(dateEpochDay: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM unlock_events
        WHERE challengeType = :challengeType AND dateEpochDay = :dateEpochDay
    """)
    suspend fun getChallengeCompletionCount(challengeType: String, dateEpochDay: Long): Int
}