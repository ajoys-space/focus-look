# Implementation Plan - Fix Unresolved reference: composeCompiler

The project is failing to sync because the `composeCompiler` plugin alias is used in `build.gradle.kts` files but is not defined in the `gradle/libs.versions.toml` file. This plan will fix the resolution issue and upgrade the project to Kotlin 2.0.21 to use the modern Compose Compiler Gradle plugin.

## User Review Required

> [!IMPORTANT]
> This plan upgrades Kotlin from `1.9.24` to `2.0.21`. This is required to use the official `org.jetbrains.kotlin.plugin.compose` plugin which is referenced in your build files. This change also requires updating the KSP version to match.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Update `kotlin` version to `2.0.21`.
- Update `ksp` version to `2.0.21-1.0.28`.
- Add `composeCompiler` to the `[plugins]` section.

#### [MODIFY] [build.gradle.kts (root)](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/build.gradle.kts)
- Add `kotlin-serialization` to the plugins block for consistency (it's used in the app module but missing from the root).

## Verification Plan

### Automated Tests
- Run Gradle sync to verify that the `composeCompiler` reference is resolved.
- Run `./gradlew assembleDebug` to ensure the project builds successfully with Kotlin 2.0.21.

### Manual Verification
- Verify that the IDE no longer shows the "Unresolved reference" error in `build.gradle.kts`.
