package com.example.fallguard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fallguard.ui.theme.*
import com.example.fallguard.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onSimulateFall: () -> Unit,
    vmFactory: androidx.lifecycle.ViewModelProvider.Factory? = null
) {
    val vm: HomeViewModel = if (vmFactory != null) viewModel(factory = vmFactory) else viewModel()
    val state by vm.state.collectAsState()

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerPulse"
    )
    val innerScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerPulse"
    )

    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Welcome back,", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        state.userName.ifBlank { "Friend" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                TextButton(onClick = onOpenSettings) {
                    Text("Settings", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pulsing monitoring circle
            Box(contentAlignment = Alignment.Center) {
                // Outermost ring
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(outerScale)
                        .clip(CircleShape)
                        .background(TealContainer.copy(alpha = 0.35f))
                )
                // Middle ring
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(innerScale)
                        .clip(CircleShape)
                        .background(TealContainer.copy(alpha = 0.6f))
                )
                // Core circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(TealPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Monitoring Active",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
            Text(
                "Your phone is watching for falls",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status cards
            StatusCard(
                title = "Emergency Contacts",
                subtitle = if (state.contacts.isEmpty()) "No contacts configured"
                           else "${state.contacts.size} contact${if (state.contacts.size == 1) "" else "s"} configured",
                statusOk = state.contacts.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatusCard(
                title = "Sensor Status",
                subtitle = "Accelerometer & gyroscope active",
                statusOk = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatusCard(
                title = "Notifications",
                subtitle = "Push notifications enabled",
                statusOk = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Simulate fall (for testing)
            OutlinedButton(
                onClick = onSimulateFall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TealPrimary)
                )
            ) {
                Text("Simulate Fall (Test)", color = TealPrimary, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    statusOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (statusOk) GreenSuccess else RedAlert)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
