package com.focuslock.app.di

import com.focuslock.app.data.repository.FocusLockRepository
import com.focuslock.app.data.repository.FocusLockRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Binds tells Hilt: "whenever something asks for FocusLockRepository,
 * give them a FocusLockRepositoryImpl". This is the one line that makes
 * the whole interface/implementation split actually work at runtime —
 * without it, Hilt has no idea which concrete class satisfies the interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFocusLockRepository(
        impl: FocusLockRepositoryImpl
    ): FocusLockRepository
}