package com.focuslock.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FocusLockApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Ensure WorkManager is using our Hilt configuration immediately.
        // This is critical for real devices like Vivo where lazy-init can sometimes
        // bypass custom configurations.
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            Log.d("FocusLockApp", "WorkManager manual init success")
        } catch (ignored: Exception) {
            // Already initialized via lazy-init Provider interface
            Log.d("FocusLockApp", "WorkManager already ready")
        }
    }
}