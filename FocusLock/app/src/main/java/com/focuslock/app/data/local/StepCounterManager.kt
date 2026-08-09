package com.focuslock.app.data.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android's TYPE_STEP_COUNTER sensor. This sensor reports a running
 * total of steps since the last device reboot (NOT since we started
 * listening) — so we capture a baseline on first reading and report deltas
 * from there, which is what the Walking Challenge actually needs.
 *
 * HONEST LIMITATION: not all devices have this sensor, and Android
 * emulators do not simulate it. hasStepSensor() must be checked before
 * relying on this class; the UI falls back gracefully when false.
 */
@Singleton
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager
        get() = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor?
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    fun hasStepSensor(): Boolean = stepSensor != null

    /**
     * Emits the number of steps taken SINCE this flow was first collected
     * (not the device's lifetime total). Cancels the sensor listener
     * automatically when the collecting coroutine is cancelled, via
     * awaitClose — standard callbackFlow cleanup pattern.
     */
    fun stepsSinceStart(): Flow<Int> = callbackFlow {
        val sensor = stepSensor
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        var baseline: Float? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val totalSinceReboot = event.values[0]
                if (baseline == null) {
                    baseline = totalSinceReboot
                }
                val stepsSinceStart = (totalSinceReboot - (baseline ?: totalSinceReboot)).toInt()
                trySend(stepsSinceStart)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}