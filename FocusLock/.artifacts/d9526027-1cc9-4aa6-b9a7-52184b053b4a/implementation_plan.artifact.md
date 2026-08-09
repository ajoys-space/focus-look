# Fix Unresolved Reference 'test' in Build

The build error `Unresolved reference 'test'` occurs because several test files are located in the `src/main/java` directory instead of the appropriate `src/test/java` or `src/androidTest/java` directories. Dependencies like `androidx.test`, `junit`, and `truth` are defined using `testImplementation` or `androidTestImplementation`, making them unavailable to the main source set.

## Proposed Changes

### [app] Component

We will move the misplaced test files to their correct source sets.

#### [MODIFY] [FocusLockDatabaseTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/main/java/com/focuslock/app/data/local/FocusLockDatabaseTest.kt)
- Move to [FocusLockDatabaseTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/androidTest/java/com/focuslock/app/data/local/FocusLockDatabaseTest.kt)

#### [MODIFY] [StreakCalculationTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/main/java/com/focuslock/app/data/repository/StreakCalculationTest.kt)
- Move to [StreakCalculationTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/test/java/com/focuslock/app/data/repository/StreakCalculationTest.kt)

#### [MODIFY] [UnlockExpiryTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/main/java/com/focuslock/app/data/repository/UnlockExpiryTest.kt)
- Move to [UnlockExpiryTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/test/java/com/focuslock/app/data/repository/UnlockExpiryTest.kt)

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build now passes.
- Run unit tests: `./gradlew :app:testDebugUnitTest`.
- Run instrumented tests (if a device is available): `./gradlew :app:connectedDebugAndroidTest`.

### Manual Verification
- Verify that the files are no longer in `src/main/java` and are correctly placed in `src/test/java` or `src/androidTest/java`.
