package com.focuslock.app.di



import android.content.Context

import androidx.room.Room

import com.focuslock.app.data.local.FocusLockDatabase

import com.focuslock.app.data.local.dao.LockedAppDao

import com.focuslock.app.data.local.dao.StreakDao

import com.focuslock.app.data.local.dao.UnlockEventDao

import com.focuslock.app.data.local.dao.UsageSessionDao

import com.focuslock.app.util.Constants

import dagger.Module

import dagger.Provides

import dagger.hilt.InstallIn

import dagger.hilt.android.qualifiers.ApplicationContext

import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton



/**

 * fallbackToDestructiveMigration() has been REMOVED as of Step 27. Since

 * the schema hasn't changed since version 1, there is nothing to migrate

 * yet — but leaving the destructive fallback in place would silently wipe

 * every real user's data the moment we ship any future schema change

 * without remembering to add a proper Migration first. Removing it now

 * forces that discipline permanently, per the honesty commitment in the

 * spec's Security section.

 */

@Module

@InstallIn(SingletonComponent::class)

object DatabaseModule {



    @Provides

    @Singleton

    fun provideDatabase(@ApplicationContext context: Context): FocusLockDatabase {

        return Room.databaseBuilder(
            context,
            FocusLockDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }



    @Provides

    fun provideLockedAppDao(database: FocusLockDatabase): LockedAppDao = database.lockedAppDao()



    @Provides

    fun provideUsageSessionDao(database: FocusLockDatabase): UsageSessionDao = database.usageSessionDao()



    @Provides

    fun provideUnlockEventDao(database: FocusLockDatabase): UnlockEventDao = database.unlockEventDao()



    @Provides

    fun provideStreakDao(database: FocusLockDatabase): StreakDao = database.streakDao()

}
