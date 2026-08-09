package com.focuslock.app.data.local

import android.content.Context
import android.net.Uri
import com.focuslock.app.data.model.LockedAppEntity
import com.focuslock.app.data.repository.FocusLockRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val lockedApps: List<BackupLockedApp>,
    val exportedAtMillis: Long
)

@Serializable
data class BackupLockedApp(
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val unlockDurationMinutes: Int
)

/**
 * Exports/imports locked-app settings (NOT usage history — that's
 * device-specific historical data, not really meaningful to restore onto
 * a different day) as local JSON. This is a genuinely simple, honest
 * implementation — no cloud sync, no account system; the user manages
 * the file themselves via their device's file picker.
 */
@Singleton
class BackupHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FocusLockRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportToUri(uri: Uri): Result<Unit> = runCatching {
        val lockedApps = repository.getAllLockedApps().first()
        val backup = BackupData(
            lockedApps = lockedApps.map {
                BackupLockedApp(it.packageName, it.appName, it.dailyLimitMinutes, it.unlockDurationMinutes)
            },
            exportedAtMillis = System.currentTimeMillis()
        )
        val jsonString = json.encodeToString(backup)

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray())
        } ?: throw IllegalStateException("Could not open output stream for backup")
    }

    suspend fun importFromUri(uri: Uri): Result<Int> = runCatching {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().decodeToString()
        } ?: throw IllegalStateException("Could not open input stream for restore")

        val backup = json.decodeFromString<BackupData>(jsonString)

        backup.lockedApps.forEach { app ->
            repository.addOrUpdateLockedApp(
                LockedAppEntity(
                    packageName = app.packageName,
                    appName = app.appName,
                    dailyLimitMinutes = app.dailyLimitMinutes,
                    unlockDurationMinutes = app.unlockDurationMinutes
                )
            )
        }

        backup.lockedApps.size
    }
}