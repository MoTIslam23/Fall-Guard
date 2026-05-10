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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fallguard.ui.theme.*
import com.example.fallguard.viewmodel.FallCountdownViewModel
import kotlinx.coroutines.delay

@Composable
fun FallCountdownScreen(
    onCancel: () -> Unit,
    onAlertSent: () -> Unit,
    vmFactory: androidx.lifecycle.ViewModelProvider.Factory? = null
) {
    val vm: FallCountdownViewModel = if (vmFactory != null) viewModel(factory = vmFactory) else viewModel()

    var secondsRemaining by remember { mutableIntStateOf(15) }

    // Pulsing red circle animation
    val infiniteTransition = rememberInfiniteTransition(label = "redPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        vm.startCountdown()
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        vm.sendAlerts(vm.incident.value?.incident_id ?: "")
        onAlertSent()
    }

    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // Badge
            Box(
                modifier = Modifier
                    .background(RedAlertLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "POSSIBLE FALL DETECTED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAlert,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Are you okay?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Countdown circle
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(RedAlert.copy(alpha = 0.18f))
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(RedAlert),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$secondsRemaining",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Text(
                            "seconds",
                            fontSize = 14.sp,
                            color = SurfaceWhite.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "If you don't respond within $secondsRemaining seconds, we'll automatically\nnotify your emergency contact",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    vm.cancel()
                    onCancel()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
            ) {
                Text("I'm okay!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    vm.immediateHelp()
                    onAlertSent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
            ) {
                Text("I need help!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
