package com.example.fallguard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fallguard.data.LocalUserStore
import com.example.fallguard.navigation.FallGuardNavGraph
import com.example.fallguard.sensor.FallDetectionService
import com.example.fallguard.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localStore = LocalUserStore(applicationContext)
        SessionStore.currentUserId = localStore.getUserId()

        val factory = ViewModelFactory(localStore)

        setContent {
            FallGuardNavGraph(
                factory = factory,
                startOnboarding = !localStore.isOnboardingComplete()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        startFallDetectionService()
    }

    private fun startFallDetectionService() {
        val intent = Intent(this, FallDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
