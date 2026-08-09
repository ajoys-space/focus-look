package com.focuslock.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focuslock.app.data.local.UsageStatsHelper
import com.focuslock.app.data.model.UsageSessionEntity
import com.focuslock.app.data.repository.FocusLockRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pulls only NEW usage sessions since the last successful sync, using a
 * persisted timestamp (fixes the duplicate-row issue flagged in Step 10).
 */
@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val usageStatsHelper: UsageStatsHelper,
    private val repository: FocusLockRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            var lastSync = repository.getLastUsageSyncMillis()

            // First-ever run: default to start of today rather than epoch 0,
            // to avoid pulling a device's entire usage history at once.
            if (lastSync == 0L) {
                lastSync = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }

            val sessions = usageStatsHelper.getSessionsBetween(lastSync, now)
            val lockedPackages = repository.getAllEnabledLockedApps().first().map { it.packageName }

            sessions
                .filter { it.packageName in lockedPackages }
                .forEach { session ->
                    val dateEpochDay = Instant.ofEpochMilli(session.startTimeMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toEpochDay()

                    repository.recordUsageSession(
                        UsageSessionEntity(
                            packageName = session.packageName,
                            startTimeMillis = session.startTimeMillis,
                            endTimeMillis = session.endTimeMillis,
                            durationMillis = session.durationMillis,
                            dateEpochDay = dateEpochDay
                        )
                    )
                }

            repository.setLastUsageSyncMillis(now)
            repository.recomputeTodayStreakStatus()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}