# Walkthrough - Fix Unresolved Reference 'test'

The build error `Unresolved reference 'test'` was resolved by moving test files from the main source set to their appropriate test source sets.

## Changes

### [app] Component

Moved the following files from `src/main/java` to their correct locations:

- [FocusLockDatabaseTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/androidTest/java/com/focuslock/app/data/local/FocusLockDatabaseTest.kt) (Instrumented Test)
- [StreakCalculationTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/test/java/com/focuslock/app/data/repository/StreakCalculationTest.kt) (Unit Test)
- [UnlockExpiryTest.kt](file:///C:/Users/Ajay%20Mugunthan%20S/AndroidStudioProjects/FocusLock/app/src/test/java/com/focuslock/app/data/repository/UnlockExpiryTest.kt) (Unit Test)

These files were causing compilation errors because they imported testing libraries (JUnit, Truth, AndroidX Test) which are only available in the `test` and `androidTest` configurations.

## Verification Results

### Automated Tests

- **Build**: Successfully executed `./gradlew :app:assembleDebug`.
- **Unit Tests**: Successfully executed `./gradlew :app:testDebugUnitTest`.
    - Total tests: 10 passed, 0 failed.

> [!NOTE]
> The instrumented test `FocusLockDatabaseTest` was moved to `src/androidTest`. To run it, you would need a connected device or emulator and run `./gradlew :app:connectedDebugAndroidTest`.
