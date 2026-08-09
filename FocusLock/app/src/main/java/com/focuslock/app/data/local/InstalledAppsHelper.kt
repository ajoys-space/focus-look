package com.focuslock.app.data.local

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Represents one installed, launchable app — what the App Selection screen displays. */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap
)

/**
 * Queries the device's installed launchable apps. Requires QUERY_ALL_PACKAGES
 * (declared in Step 4's manifest) since Android 11+ hides most package
 * queries by default without it.
 */
@Singleton
class InstalledAppsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns only apps with a launcher icon (i.e. apps the user can
     * actually open from the home screen) — filters out system services,
     * background-only components, etc. that would just clutter the list.
     * Runs on Dispatchers.Default since querying PackageManager for every
     * installed app can be a genuinely slow operation on some devices.
     */
    suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedApps = pm.queryIntentActivities(intent, 0)

        resolvedApps
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName } // Never let the user "lock" Focus Lock itself
            .mapNotNull { appInfo ->
                try {
                    InstalledApp(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        icon = pm.getApplicationIcon(appInfo).toBitmap().asImageBitmap()
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
            .sortedBy { it.appName.lowercase() }
    }
}