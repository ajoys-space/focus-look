# Fix Compose Compiler Plugin Issue

The project is using Kotlin 2.0.0, which requires the new Compose Compiler Gradle plugin. While the plugin is defined in the Version Catalog and the root build file, it is not applied in the app-level `build.gradle.kts`.

## Proposed Changes

### App Module

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/build.gradle.kts)

- Apply the Compose Compiler Gradle plugin.
- Ensure `composeOptions` (specifically `kotlinCompilerExtensionVersion`) is not present as it is deprecated in Kotlin 2.0+.

## Verification Plan

### Automated Tests
- Run `./gradlew build` to ensure the project compiles and the plugin is correctly applied.

### Manual Verification
- Perform a Gradle Sync in Android Studio to verify the error is resolved.
