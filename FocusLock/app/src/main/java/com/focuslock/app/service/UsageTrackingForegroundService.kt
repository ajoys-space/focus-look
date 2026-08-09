package com.focuslock.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focuslock.app.MainActivity
import com.focuslock.app.R
import com.focuslock.app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class UsageTrackingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(Constants.USAGE_TRACKING_NOTIFICATION_ID, buildNotification())
        schedulePeriodicSync()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.USAGE_TRACKING_CHANNEL_ID,
                "Usage Tracking",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps Focus Lock running to track app usage"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        return NotificationCompat.Builder(this, Constants.USAGE_TRACKING_CHANNEL_ID)
            .setContentTitle("Focus Lock is active")
            .setContentText("Tracking your app usage to help you stay focused")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    /**
     * setRequiresBatteryNotLow(true) skips syncing while the device is in
     * low-battery state — the 15-minute periodic sync isn't urgent enough
     * to justify draining an already-low battery; usage will simply catch
     * up on the next cycle once charging resumes or battery recovers.
     * Exponential backoff on failure avoids hammering retries aggressively.
     */
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<UsageTrackingWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "usage_tracking_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UsageTrackingForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}