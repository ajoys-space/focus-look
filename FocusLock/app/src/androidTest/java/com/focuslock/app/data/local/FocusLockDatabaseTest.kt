package com.focuslock.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.focuslock.app.data.model.LockedAppEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Runs on a real device/emulator (not the JVM) since Room's SQLite
 * implementation requires the Android platform. Uses an in-memory database
 * so tests don't leave files behind or interfere with the real app's data.
 */
@RunWith(AndroidJUnit4::class)
class FocusLockDatabaseTest {

    private lateinit var database: FocusLockDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FocusLockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveLockedApp() = runBlocking {
        val app = LockedAppEntity(
            packageName = "com.example.testapp",
            appName = "Test App",
            dailyLimitMinutes = 60
        )

        database.lockedAppDao().insert(app)

        val retrieved = database.lockedAppDao().getByPackageName("com.example.testapp")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.appName).isEqualTo("Test App")
        assertThat(retrieved?.dailyLimitMinutes).isEqualTo(60)
    }

    @Test
    fun deletingLockedAppCascadesUsageSessions() = runBlocking {
        val app = LockedAppEntity(
            packageName = "com.example.testapp",
            appName = "Test App",
            dailyLimitMinutes = 60
        )
        database.lockedAppDao().insert(app)

        val sessionId = database.usageSessionDao().insert(
            com.focuslock.app.data.model.UsageSessionEntity(
                packageName = "com.example.testapp",
                startTimeMillis = 0,
                endTimeMillis = 60000,
                durationMillis = 60000,
                dateEpochDay = 100
            )
        )
        assertThat(sessionId).isGreaterThan(0)

        // Confirms the CASCADE foreign key Actually works —
        // deleting the parent app should remove its usage history too.
        database.lockedAppDao().deleteByPackageName("com.example.testapp")

        val remainingSessions = database.usageSessionDao()
            .getSessionsForApp("com.example.testapp").first()
        assertThat(remainingSessions).isEmpty()
    }

    @Test
    fun updateUsageSessionCorrectly() = runBlocking {
        val app = LockedAppEntity("com.test", "Test", 10)
        database.lockedAppDao().insert(app)

        val id = database.usageSessionDao().insert(
            com.focuslock.app.data.model.UsageSessionEntity(
                packageName = "com.test",
                startTimeMillis = 1000,
                endTimeMillis = 2000,
                durationMillis = 1000,
                dateEpochDay = 10
            )
        )

        val updated = com.focuslock.app.data.model.UsageSessionEntity(
            id = id,
            packageName = "com.test",
            startTimeMillis = 1000,
            endTimeMillis = 3000,
            durationMillis = 2000,
            dateEpochDay = 10
        )
        database.usageSessionDao().update(updated)

        val sessions = database.usageSessionDao().getSessionsForApp("com.test").first()
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].durationMillis).isEqualTo(2000)
    }

    @Test
    fun getAllLockedAppsReturnsInsertedApps() = runBlocking {
        database.lockedAppDao().insertAll(
            listOf(
                LockedAppEntity("com.example.a", "App A", 30),
                LockedAppEntity("com.example.b", "App B", 60)
            )
        )

        val allApps = database.lockedAppDao().getAllLockedApps().first()
        assertThat(allApps).hasSize(2)
    }
}