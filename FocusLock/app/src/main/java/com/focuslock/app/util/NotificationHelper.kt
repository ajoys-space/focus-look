package com.focuslock.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.focuslock.app.MainActivity
import com.focuslock.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fires the user-facing notifications from spec item 13. Separate
 * notification channel from the Foreground Service's silent one (Step 11)
 * since these are meant to actually get the user's attention, not blend
 * into the background — different IMPORTANCE level reflects that.
 *
 * Respects the user's notification preference (Step 7/23's Settings toggle)
 * by checking userPreferences before firing — callers don't need to
 * remember this check themselves.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val ALERTS_CHANNEL_ID = "focus_lock_alerts"
        private const val LIMIT_REACHED_NOTIFICATION_ID = 2001
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you reach an app's daily limit"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * "You've reached your limit" + "Complete challenge to continue" —
     * fired the moment the Accessibility Service decides to block an app.
     */
    fun showLimitReachedNotification(appName: String) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = getAppIconBitmap()

        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setContentTitle("You've reached your limit")
            .setContentText("$appName is locked. Complete a challenge to continue.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Guard: POST_NOTIFICATIONS may not be granted (Android 13+); posting
        // without checking would crash with a SecurityException.
        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context)
                .notify(LIMIT_REACHED_NOTIFICATION_ID, notification)
        }
    }

    /** "Take a break" — a softer, encouraging nudge, distinct from the hard block alert. */
    fun showTakeABreakNotification(appName: String) {
        val largeIcon = getAppIconBitmap()
        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setContentTitle("Take a break")
            .setContentText("You've been spending a lot of time on $appName today.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context)
                .notify(LIMIT_REACHED_NOTIFICATION_ID + 1, notification)
        }
    }

    private fun getAppIconBitmap(): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher) ?: return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // No runtime check needed below API 33
        }
    }
}