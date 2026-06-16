package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class StepSensorManager(
    context: Context,
    private val onStepCounted: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Hardware sensors
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Accelerometer algorithm states
    private var lastMagnitude = 0f
    private var isPeak = false
    private var lastStepTimeNs = 0L
    private val stepThresholdMagnitude = 12.0f // Threshold magnitude to register a step
    private val stepCooldownNs = 350_000_000L // 350ms cooldown between steps

    // To prevent sudden huge increments from TYPE_STEP_COUNTER
    private var initialStepCount = -1f

    // Public property to track last active timestamp
    @Volatile
    var lastActiveTimestamp: Long = System.currentTimeMillis()

    fun startTracking() {
        // 1. Try registering step detector (preferred, low power)
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepSensorManager", "Registered TYPE_STEP_DETECTOR")
        }
        
        // 2. Try registering step counter as fallback or companion
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepSensorManager", "Registered TYPE_STEP_COUNTER")
        }

        // 3. Register accelerometer as the fallback physical motion sensor (always works, even in emulators/streaming UI!)
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepSensorManager", "Registered TYPE_ACCELEROMETER")
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        Log.d("StepSensorManager", "Unregistered all sensor listeners")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                // Returns 1.0 when a step is detected
                if (event.values.isNotEmpty() && event.values[0] > 0) {
                    lastActiveTimestamp = System.currentTimeMillis()
                    onStepCounted(1)
                    Log.d("StepSensorManager", "Step detected via TYPE_STEP_DETECTOR")
                }
            }
            
            Sensor.TYPE_STEP_COUNTER -> {
                // Returns the cumulative step count since the last reboot
                val totalSteps = event.values[0]
                if (initialStepCount < 0) {
                    initialStepCount = totalSteps
                } else {
                    val deltaSteps = (totalSteps - initialStepCount).toInt()
                    if (deltaSteps > 0) {
                        lastActiveTimestamp = System.currentTimeMillis()
                        onStepCounted(deltaSteps)
                        initialStepCount = totalSteps // Reset baseline
                        Log.d("StepSensorManager", "Steps counted via TYPE_STEP_COUNTER: $deltaSteps")
                    }
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Fallback custom peak detection for steps based on accelerometers (great for emulators/shaking)
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                val magnitude = sqrt(x * x + y * y + z * z)
                val currentTimeNs = event.timestamp

                // If magnitude peaks above 12.0 m/s^2 and cooling down has passed
                if (magnitude > stepThresholdMagnitude && !isPeak) {
                    if (currentTimeNs - lastStepTimeNs > stepCooldownNs) {
                        isPeak = true
                        lastStepTimeNs = currentTimeNs
                        lastActiveTimestamp = System.currentTimeMillis()
                        onStepCounted(1)
                        Log.d("StepSensorManager", "Step counted via TYPE_ACCELEROMETER magnitude: $magnitude")
                    }
                } else if (magnitude < stepThresholdMagnitude - 1.5f) {
                    isPeak = false
                }
                lastMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
