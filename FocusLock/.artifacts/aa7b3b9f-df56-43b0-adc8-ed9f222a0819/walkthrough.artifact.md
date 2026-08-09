# Walkthrough - Fixed Compose Compiler Plugin Issue

I have resolved the Gradle sync error related to the Compose Compiler plugin requirement in Kotlin 2.0.

## Changes Made

### App Module

#### [app/build.gradle.kts](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/build.gradle.kts)

Applied the `composeCompiler` plugin alias in the `plugins` block.

```diff
 plugins {
     alias(libs.plugins.android.application)
     alias(libs.plugins.kotlin.android)
     alias(libs.plugins.hilt.android.plugin)
     alias(libs.plugins.ksp)
     alias(libs.plugins.kotlin.serialization)
+    alias(libs.plugins.composeCompiler)
 }
```

## Verification Results

### Automated Tests
- Successfully performed a **Gradle Sync**, which now completes without errors.
