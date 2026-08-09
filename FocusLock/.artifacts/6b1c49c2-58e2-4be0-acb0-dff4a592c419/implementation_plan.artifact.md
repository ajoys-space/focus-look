# Fix Unresolved Reference: composeCompiler

The project is failing to sync because `libs.plugins.composeCompiler` is referenced in the build files but is not defined in the `gradle/libs.versions.toml` file.

The `composeCompiler` Gradle plugin is the modern way to manage the Compose compiler, but it requires Kotlin 2.0.0 or higher. Since the project is currently on Kotlin 1.9.24, I will upgrade the project to Kotlin 2.0.21 and add the missing plugin definition.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Upgrade `kotlin` version from `1.9.24` to `2.0.21`.
- Upgrade `ksp` version from `1.9.24-1.0.20` to `2.0.21-1.0.25` to match the new Kotlin version.
- Add `composeCompiler` plugin definition in the `[plugins]` section.

## Verification Plan

### Automated Tests
- Run Gradle sync to ensure the "Unresolved reference" error is gone.
- Run `./gradlew assembleDebug` to verify the project builds successfully with Kotlin 2.0.

### Manual Verification
- Verify that the IDE no longer shows red markers on `libs.plugins.composeCompiler` in `build.gradle.kts` files.
