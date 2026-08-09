# Focus Lock ProGuard/R8 rules
# These are written now (Step 25) so they're ready and tested before
# Step 27's release build turns on isMinifyEnabled — adding them retroactively
# after a release build silently breaks is much harder to debug than
# having them verified early.

# ---- Room ----
# Room's generated code and entities must survive minification/obfuscation
# for reflection-based database operations to keep working correctly.
-keep class com.focuslock.app.data.model.** { *; }
-keep class com.focuslock.app.data.local.FocusLockDatabase { *; }
-dontwarn androidx.room.paging.**

# ---- Hilt ----
# Hilt-generated components rely on specific class/method names that
# R8 must not rename or strip.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}
-keep,allowobfuscation @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ---- WorkManager / Hilt Workers ----
-keep class * extends androidx.work.CoroutineWorker
-keep,allowobfuscation @androidx.hilt.work.HiltWorker class *

# ---- kotlinx.serialization ----
# Serializable data classes need their structure preserved for the
# backup/restore feature (Step 23) to correctly encode/decode JSON.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.focuslock.app.data.local.BackupData { *; }
-keep,includedescriptorclasses class com.focuslock.app.data.local.BackupLockedApp { *; }
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }

# ---- Accessibility Service ----
# Must never be stripped or renamed — the system references this class
# by its exact declared name from the Manifest's <service> entry.
-keep class com.focuslock.app.service.AppBlockerAccessibilityService { *; }
-keep class com.focuslock.app.service.UsageTrackingForegroundService { *; }
-keep class com.focuslock.app.service.BootReceiver { *; }

# ---- General AndroidX / Compose safety ----
-dontwarn kotlinx.coroutines.**
-keepattributes Signature
-keepattributes *Annotation*