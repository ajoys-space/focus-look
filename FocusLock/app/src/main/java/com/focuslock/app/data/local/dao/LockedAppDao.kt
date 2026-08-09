package com.focuslock.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focuslock.app.data.model.LockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: LockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<LockedAppEntity>)

    @Update
    suspend fun update(app: LockedAppEntity)

    @Delete
    suspend fun delete(app: LockedAppEntity)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    // Flow so the UI (Home screen, App Selection screen) auto-updates
    // whenever the tracked-app list changes, with no manual refresh needed.
    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE isEnabled = 1")
    fun getAllEnabledLockedApps(): Flow<List<LockedAppEntity>>

    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): LockedAppEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE packageName = :packageName)")
    suspend fun isAppLocked(packageName: String): Boolean
}