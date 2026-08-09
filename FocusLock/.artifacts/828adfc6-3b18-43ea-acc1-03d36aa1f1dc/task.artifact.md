# Tasks - Fix Build Configuration Warnings and Errors

- [x] Update `gradle/libs.versions.toml` with latest versions and new plugins
- [x] Refactor root `build.gradle.kts` to use Version Catalog aliases
- [x] Refactor `:app/build.gradle.kts`:
    - [x] Update SDK versions (compileSdk/targetSdk to 35)
    - [x] Migrate all hardcoded dependencies to `libs` references
    - [x] Update Compose configuration (remove `composeOptions`, add plugin)
    - [x] Apply minor formatting fixes
- [x] Run Gradle Sync and verify build
