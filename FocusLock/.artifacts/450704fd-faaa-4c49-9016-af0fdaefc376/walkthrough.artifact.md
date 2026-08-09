# Walkthrough - Fixed Unresolved Reference: composeCompiler

I have fixed the "Unresolved reference: composeCompiler" error by properly defining the Compose compiler plugin in your version catalog and upgrading the project to Kotlin 2.0.

## Changes

### Build Configuration

#### [libs.versions.toml](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Upgraded Kotlin from `1.9.24` to `2.0.0`.
- Upgraded KSP to `2.0.0-1.0.21` to match the new Kotlin version.
- Added the `composeCompiler` plugin definition using the new Kotlin 2.0+ plugin ID: `org.jetbrains.kotlin.plugin.compose`.

#### [app/build.gradle.kts](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/build.gradle.kts)
- Applied the `composeCompiler` plugin in the `plugins` block.

## Verification Results

### Automated Tests
- **Gradle Sync**: Successful. The unresolved reference error is gone, and the project is now using the modern Compose compiler plugin.

> [!NOTE]
> By upgrading to Kotlin 2.0, your project now uses the bundled Compose compiler, which eliminates the need to manually manage compatibility between Kotlin and Compose versions.
