package com.example.fallguard.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.fallguard.SessionStore
import com.example.fallguard.network.ApiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ---------------------------------------------------------
// Foreground service that continuously monitors device sensors.
// When a fall pattern is confirmed, it starts the backend countdown.
// ---------------------------------------------------------

class FallDetectionService : Service(), SensorEventListener {

    companion object {
        private const val CHANNEL_ID = "fall_detection_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private val detector = FallDetector(
        onPotentialFallConfirmed = {
            startBackendCountdown()
        }
    )

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        createChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification("Monitoring sensors for possible falls")
        )

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val now = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                detector.updateAccelerometer(
                    event.values[0],
                    event.values[1],
                    event.values[2],
                    now
                )
            }

            Sensor.TYPE_GYROSCOPE -> {
                detector.updateGyroscope(
                    event.values[0],
                    event.values[1],
                    event.values[2],
                    now
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun startBackendCountdown() {
        val userId = SessionStore.currentUserId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiProvider.api.startCountdown(
                    com.example.fallguard.network.StartCountdownRequest(
                        user_id = userId,
                        seconds = 15
                    )
                )
                updateNotification("Possible fall detected. Countdown started.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FallGuard")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fall Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}