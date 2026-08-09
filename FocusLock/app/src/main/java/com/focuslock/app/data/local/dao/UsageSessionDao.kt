package com.focuslock.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.focuslock.app.data.model.UsageSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSessionDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insert(session: UsageSessionEntity): Long

    @Update
    suspend fun update(session: UsageSessionEntity)

    // Sum of usage for one app on one specific day — this is the core query
    // the blocking logic calls constantly: "has this app exceeded its limit today?"
    @Query("""
        SELECT COALESCE(SUM(durationMillis), 0) FROM usage_sessions
        WHERE packageName = :packageName AND dateEpochDay = :dateEpochDay
    """)
    suspend fun getTotalUsageMillisForAppOnDay(packageName: String, dateEpochDay: Long): Long

    @Query("""
        SELECT COALESCE(SUM(durationMillis), 0) FROM usage_sessions
        WHERE packageName = :packageName AND dateEpochDay = :dateEpochDay AND id != :excludeId
    """)
    suspend fun getTotalUsageMillisForAppOnDayExcludingSession(packageName: String, dateEpochDay: Long, excludeId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(durationMillis), 0) FROM usage_sessions
        WHERE dateEpochDay = :dateEpochDay
    """)
    fun getTotalUsageMillisForDay(dateEpochDay: Long): Flow<Long>

    // Powers the Statistics screen's daily/weekly/monthly graphs
    @Query("""
        SELECT * FROM usage_sessions
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY startTimeMillis ASC
    """)
    fun getSessionsBetweenDates(startEpochDay: Long, endEpochDay: Long): Flow<List<UsageSessionEntity>>

    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName ORDER BY startTimeMillis DESC")
    fun getSessionsForApp(packageName: String): Flow<List<UsageSessionEntity>>
}