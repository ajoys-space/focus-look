# Fix Unresolved Reference: composeCompiler

The project is failing to sync because the `composeCompiler` plugin and several library dependencies used in `app/build.gradle.kts` are missing from the `libs.versions.toml` file. Additionally, the project is using Kotlin 1.9.24, but attempting to use the modern Compose Compiler Gradle plugin, which is standard for Kotlin 2.0+.

## User Review Required

> [!IMPORTANT]
> This plan involves upgrading Kotlin from **1.9.24** to **2.0.21** to support the `org.jetbrains.kotlin.plugin.compose` Gradle plugin. This is the modern way to configure the Compose compiler and is required for the `alias(libs.plugins.composeCompiler)` syntax used in your build files.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Upgrade `kotlin` version to `2.0.21`.
- Upgrade `ksp` version to `2.0.21-1.0.25`.
- Add `junit`, `androidxTestExtJunit`, `espressoCore`, and `composeUiTestManifest` versions and library definitions.
- Add `composeCompiler` plugin definition.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/build.gradle.kts)
- Ensure the plugin is applied correctly (already present in the file).
- No major changes needed here if the version catalog is fixed.

## Verification Plan

### Automated Tests
- Run `./gradlew sync` (or trigger Sync in Android Studio) to verify that the unresolved reference is resolved and the project structure is valid.
- Build the project to ensure compatibility between Kotlin 2.0.21 and existing dependencies (Hilt, Room, etc.).

### Manual Verification
- Check that the `composeCompiler` block can be added to `app/build.gradle.kts` without errors (optional but good for future-proofing).
