package com.focuslock.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restarts the Foreground Service after device reboot. Accessibility
 * Services are automatically re-enabled by Android itself if the user
 * granted the permission previously — that part needs no code from us.
 * What we DO need to restart manually is our Foreground Service and its
 * WorkManager scheduling, since those don't survive a reboot on their own.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UsageTrackingForegroundService.start(context)
        }
    }
}