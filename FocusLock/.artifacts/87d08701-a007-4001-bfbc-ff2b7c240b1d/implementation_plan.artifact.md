# Fix KSP Build Error: ArrayIndexOutOfBoundsException

The user is experiencing a build failure in KSP with the error `java.io.IOException: java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0`. This is a known issue typically caused by corruption in the KSP incremental compilation cache, especially when using Kotlin 2.0.0.

## User Review Required

> [!IMPORTANT]
> The first step involves cleaning the project, which will trigger a full rebuild. This might take some time depending on the project size.
> I will also update the KSP version to a more stable release for Kotlin 2.0.0.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Ajay Mugunthan S/AndroidStudioProjects/FocusLock/gradle/libs.versions.toml)
- Update `ksp` version from `2.0.0-1.0.21` to `2.0.0-1.0.25` to include stability fixes for Kotlin 2.0.0.

#### [MODIFY] [gradle.properties](file:///C:/Users/Ajay Mugunthan S/AndroidStudioProjects/FocusLock/gradle.properties)
- (Optional/Backup) If the issue persists, I will add `ksp.incremental=false` to disable incremental compilation for KSP, or `ksp.useKSP2=false` to use the more stable KSP1 engine. I will start by just cleaning and updating.

## Verification Plan

### Automated Tests
- Run `./gradlew clean :app:kspDebugKotlin` to verify that KSP can now process symbols without crashing.
- Run a full build `./gradlew assembleDebug` to ensure the entire project builds successfully.

### Manual Verification
- Verify that the `build` folders are recreated and KSP generates the necessary code (e.g., Room and Hilt components).
