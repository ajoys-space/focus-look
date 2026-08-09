# Implementation Plan - Resolve SDK and AGP Compatibility Issues

The recent update to the latest stable versions of Hilt and Lifecycle libraries (`1.4.0` and `2.11.0` respectively) has introduced compatibility issues. These versions require **Android SDK 37** and **Android Gradle Plugin (AGP) 9.1.0** or higher.

## User Review Required

> [!IMPORTANT]
> To resolve the metadata errors, I am proposing to upgrade the project to **SDK 37** and **AGP 9.3.0**. This follows the "Recommended action" provided in the build errors.
>
> If you prefer to stay on **SDK 35** and **AGP 8.8.2**, I would need to downgrade the following dependencies instead:
> - `androidx.hilt:hilt-navigation-compose` to `1.2.0`
> - `androidx.lifecycle` to `2.8.7` (and potentially use an earlier `compose-bom` if it's forcing a newer lifecycle version).
>
> **Proposed: Upgrade to SDK 37 and AGP 9.3.0.**

## Proposed Changes

### [Component: Version Catalog]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ajay Mugunthan S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Update `agp` version to `9.3.0`.

### [Component: Build Logic]

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Ajay Mugunthan S/AndroidStudioProjects/FocusLock/app/build.gradle.kts)
- Update `compileSdk` to `37`.
- Keep `targetSdk` at `35` (or update to `37` if you want to opt-in to the latest runtime behaviors). I will update it to `37` to resolve the warning "A newer version of targetSdk than 35 is available: 37".

## Verification Plan

### Automated Tests
- Run `Gradle Sync`.
- Run `./gradlew build` to ensure the metadata issues are resolved and the project compiles.

### Manual Verification
- Check for any new warnings in the build files regarding SDK or AGP versions.
