package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class MotionTelemetry(
    val isTracking: Boolean = false,
    val durationSeconds: Long = 0,
    val stepCount: Int = 0,
    val elevationGainMeters: Float = 0f,
    val depressionMeters: Float = 0f,
    val forwardDistanceMeters: Float = 0f,
    val lateralDistanceMeters: Float = 0f,
    val totalDistanceMeters: Float = 0f,
    val currentAltitudeMeters: Float = 0f,
    val currentHeadingDegrees: Float = 0f,
    val currentSpeedMps: Float = 0f,
    val hasBarometer: Boolean = false,
    val hasStepCounter: Boolean = false,
    val hasAccelerometer: Boolean = false,
    val isSimulationMode: Boolean = false,
    val recentElevationHistory: List<Float> = emptyList() // Last 20 points for live graph
)

class MotionTracker(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val barometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val stepDetector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _telemetry = MutableStateFlow(
        MotionTelemetry(
            hasBarometer = barometer != null,
            hasStepCounter = stepDetector != null,
            hasAccelerometer = accelerometer != null
        )
    )
    val telemetry: StateFlow<MotionTelemetry> = _telemetry.asStateFlow()

    private var initialPressure: Float? = null
    private var lastAltitude: Float? = null
    private var initialStepCount: Int? = null

    // Tracking state
    private var isTracking = false
    private var stepsAccumulated = 0
    private var elevationGain = 0f
    private var depression = 0f
    private var forwardDistance = 0f
    private var lateralDistance = 0f
    private var totalDistance = 0f
    private var sessionStartTime = 0L

    private val elevationHistory = mutableListOf<Float>()

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        sessionStartTime = System.currentTimeMillis()

        sensorManager?.let { sm ->
            accelerometer?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            barometer?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            stepDetector?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            rotationSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }

        updateState()
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        sensorManager?.unregisterListener(this)
        updateState()
    }

    fun resetSession() {
        stepsAccumulated = 0
        elevationGain = 0f
        depression = 0f
        forwardDistance = 0f
        lateralDistance = 0f
        totalDistance = 0f
        initialPressure = null
        lastAltitude = null
        initialStepCount = null
        elevationHistory.clear()
        sessionStartTime = if (isTracking) System.currentTimeMillis() else 0L
        updateState()
    }

    // Manual / Simulation injectors (useful for testing on emulator or stationary environments)
    fun simulateSteps(stepsToAdd: Int, forwardMeters: Float = 0.75f * stepsToAdd, lateralMeters: Float = 0f) {
        stepsAccumulated += stepsToAdd
        forwardDistance += forwardMeters
        lateralDistance += lateralMeters
        totalDistance += sqrt(forwardMeters.pow(2) + lateralMeters.pow(2))
        updateState()
    }

    fun simulateElevation(gain: Float, drop: Float) {
        elevationGain += gain
        depression += drop
        val currentAlt = (_telemetry.value.currentAltitudeMeters + gain - drop)
        addAltitudePoint(currentAlt)
        updateState()
    }

    private fun addAltitudePoint(alt: Float) {
        elevationHistory.add(alt)
        if (elevationHistory.size > 25) {
            elevationHistory.removeAt(0)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTracking || event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_PRESSURE -> {
                val pressureHpa = event.values[0]
                // Hypsometric formula for altitude in meters
                val altitudeM = 44330f * (1.0f - (pressureHpa / 1013.25f).pow(0.190295f))

                if (initialPressure == null) {
                    initialPressure = pressureHpa
                    lastAltitude = altitudeM
                    addAltitudePoint(altitudeM)
                } else {
                    val prevAlt = lastAltitude ?: altitudeM
                    val deltaAlt = altitudeM - prevAlt

                    // Filter out microscopic sensor noise (< 0.15m)
                    if (abs(deltaAlt) >= 0.15f) {
                        if (deltaAlt > 0) {
                            elevationGain += deltaAlt
                        } else {
                            depression += abs(deltaAlt)
                        }
                        lastAltitude = altitudeM
                        addAltitudePoint(altitudeM)
                    }
                }
                updateState()
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                stepsAccumulated += 1
                forwardDistance += 0.72f // Average human stride
                totalDistance += 0.72f
                updateState()
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val rawSteps = event.values[0].toInt()
                if (initialStepCount == null) {
                    initialStepCount = rawSteps
                } else {
                    val sessionSteps = rawSteps - (initialStepCount ?: rawSteps)
                    if (sessionSteps > stepsAccumulated) {
                        val delta = sessionSteps - stepsAccumulated
                        stepsAccumulated = sessionSteps
                        forwardDistance += delta * 0.72f
                        totalDistance += delta * 0.72f
                        updateState()
                    }
                }
            }

            Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Accelerometer based step detection and vertical movement estimation if no barometer/step counter
                val magnitude = sqrt(x * x + y * y + z * z)
                if (stepDetector == null && magnitude > 11.5f) {
                    stepsAccumulated += 1
                    forwardDistance += 0.72f
                    totalDistance += 0.72f
                }

                // If no barometer, estimate vertical displacement from vertical axis accelerations
                if (barometer == null) {
                    if (z > 2.5f) {
                        elevationGain += 0.15f
                        val currentAlt = _telemetry.value.currentAltitudeMeters + 0.15f
                        addAltitudePoint(currentAlt)
                    } else if (z < -2.5f) {
                        depression += 0.15f
                        val currentAlt = _telemetry.value.currentAltitudeMeters - 0.15f
                        addAltitudePoint(currentAlt)
                    }
                }

                // Lateral vs Forward sway tracking
                if (abs(x) > 1.2f) {
                    lateralDistance += abs(x) * 0.05f
                }
                updateState()
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                // Azimuth in degrees (0 = North, 90 = East, 180 = South, 270 = West)
                val azimuthDegrees = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                _telemetry.value = _telemetry.value.copy(currentHeadingDegrees = azimuthDegrees)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun updateState() {
        val duration = if (isTracking && sessionStartTime > 0L) {
            (System.currentTimeMillis() - sessionStartTime) / 1000L
        } else {
            _telemetry.value.durationSeconds
        }

        _telemetry.value = _telemetry.value.copy(
            isTracking = isTracking,
            durationSeconds = duration,
            stepCount = stepsAccumulated,
            elevationGainMeters = elevationGain,
            depressionMeters = depression,
            forwardDistanceMeters = forwardDistance,
            lateralDistanceMeters = lateralDistance,
            totalDistanceMeters = totalDistance,
            recentElevationHistory = elevationHistory.toList()
        )
    }
}
