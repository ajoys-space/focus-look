package com.focuslock.app.data.local

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents one continuous foreground session for a single app, computed
 * from raw UsageEvents. This is what gets converted into a UsageSessionEntity
 * and saved to Room.
 */
data class RawUsageSession(
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long
) {
    val durationMillis: Long get() = endTimeMillis - startTimeMillis
}

/**
 * Wraps Android's UsageStatsManager, which returns raw MOVE_TO_FOREGROUND /
 * MOVE_TO_BACKGROUND events rather than clean "session" objects — we do that
 * reconstruction ourselves here, since the raw API is genuinely painful to
 * work with directly.
 *
 * Requires PACKAGE_USAGE_STATS permission (special, user-granted via
 * Settings — see Step 4's PermissionUtils.hasUsageStatsPermission()).
 * If that permission isn't granted, queryEvents() silently returns nothing
 * useful — Android doesn't throw an exception, it just returns empty data,
 * which is worth knowing so we don't mistake "no permission" for "no usage".
 */
@Singleton
class UsageStatsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val usageStatsManager: UsageStatsManager
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * Reconstructs foreground sessions between two timestamps by walking
     * the raw event stream and pairing MOVE_TO_FOREGROUND with the next
     * MOVE_TO_BACKGROUND (or MOVE_TO_FOREGROUND of a different app, which
     * implicitly ends the previous session too).
     */
    fun getSessionsBetween(startTimeMillis: Long, endTimeMillis: Long): List<RawUsageSession> {
        val events = usageStatsManager.queryEvents(startTimeMillis, endTimeMillis)
        val sessions = mutableListOf<RawUsageSession>()

        // Tracks the currently "open" foreground session per package, since
        // multiple apps' events can interleave in the raw stream.
        val openSessions = mutableMapOf<String, Long>() // packageName -> startTime

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // Close any previously open session for a DIFFERENT package,
                    // since only one app can truly be foreground at a time.
                    openSessions.entries
                        .filter { it.key != pkg }
                        .forEach { (openPkg, openStart) ->
                            sessions.add(RawUsageSession(openPkg, openStart, event.timeStamp))
                        }
                    openSessions.keys.removeAll { it != pkg }
                    openSessions[pkg] = event.timeStamp
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = openSessions.remove(pkg)
                    if (start != null && event.timeStamp > start) {
                        sessions.add(RawUsageSession(pkg, start, event.timeStamp))
                    }
                }
            }
        }

        // Any session still "open" at query end (app still in foreground now)
        // gets closed at endTimeMillis so we don't lose that partial session.
        openSessions.forEach { (pkg, start) ->
            if (endTimeMillis > start) {
                sessions.add(RawUsageSession(pkg, start, endTimeMillis))
            }
        }

        return sessions.filter { it.durationMillis > 0 }
    }

    /** Convenience: sessions from the start of today (midnight, device timezone) until now. */
    fun getTodaysSessions(): List<RawUsageSession> {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return getSessionsBetween(startOfDay, System.currentTimeMillis())
    }
}