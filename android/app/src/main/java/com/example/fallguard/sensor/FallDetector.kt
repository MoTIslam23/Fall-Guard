package com.example.fallguard.sensor

import kotlin.math.sqrt

// ---------------------------------------------------------
// Fall detection engine.
// Uses accelerometer + gyroscope pattern detection:
// 1. low acceleration (free-fall-like)
// 2. impact spike
// 3. strong rotation
//
// This is the detection logic that triggers the countdown flow.
// ---------------------------------------------------------

class FallDetector(
    private val onPotentialFallConfirmed: () -> Unit
) {
    companion object {
        private const val FREE_FALL_ACCEL_THRESHOLD = 2.0f
        private const val IMPACT_ACCEL_THRESHOLD = 20.0f
        private const val ROTATION_THRESHOLD = 3.0f
        private const val FREE_FALL_MIN_DURATION_MS = 250L
        private const val IMPACT_WINDOW_MS = 2000L
    }

    private enum class State {
        IDLE,
        FREE_FALL_DETECTED
    }

    private var state = State.IDLE
    private var latestAccelMagnitude = 9.8f
    private var latestGyroMagnitude = 0f
    private var freeFallStartTime: Long? = null
    private var lastFreeFallTime: Long? = null

    fun updateAccelerometer(x: Float, y: Float, z: Float, timestampMs: Long) {
        latestAccelMagnitude = magnitude(x, y, z)
        evaluate(timestampMs)
    }

    fun updateGyroscope(x: Float, y: Float, z: Float, timestampMs: Long) {
        latestGyroMagnitude = magnitude(x, y, z)
        evaluate(timestampMs)
    }

    private fun evaluate(now: Long) {
        when (state) {
            State.IDLE -> {
                if (latestAccelMagnitude < FREE_FALL_ACCEL_THRESHOLD) {
                    if (freeFallStartTime == null) {
                        freeFallStartTime = now
                    }

                    val duration = now - (freeFallStartTime ?: now)
                    if (duration >= FREE_FALL_MIN_DURATION_MS) {
                        state = State.FREE_FALL_DETECTED
                        lastFreeFallTime = now
                    }
                } else {
                    freeFallStartTime = null
                }
            }

            State.FREE_FALL_DETECTED -> {
                val elapsedSinceFreeFall = now - (lastFreeFallTime ?: now)
                val impact = latestAccelMagnitude > IMPACT_ACCEL_THRESHOLD
                val rotation = latestGyroMagnitude > ROTATION_THRESHOLD

                if (impact && rotation) {
                    reset()
                    onPotentialFallConfirmed()
                    return
                }

                if (elapsedSinceFreeFall > IMPACT_WINDOW_MS) {
                    reset()
                }
            }
        }
    }

    private fun magnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }

    private fun reset() {
        state = State.IDLE
        latestAccelMagnitude = 9.8f
        latestGyroMagnitude = 0f
        freeFallStartTime = null
        lastFreeFallTime = null
    }
}