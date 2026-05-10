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
import com.example.fallguard.ui.theme.*

@Composable
fun AlertSentScreen(
    emsMode: Boolean = false,
    onCancel: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orangePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
                    .background(OrangeAlertLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "ALERT SENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAlert,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                if (emsMode) "Contacting EMS" else "Contacting Your\nEmergency Contacts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!emsMode) {
                Text(
                    "We've sent an alert to your contacts,\nand direct them to you.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "If someone doesn't respond in 15 seconds,\nwe'll notify EMS.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    "We've notified EMS, they should be on their way!",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pulsing orange circle
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(OrangeAlert.copy(alpha = 0.20f))
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(OrangeAlert.copy(alpha = 0.40f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(OrangeAlert)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onCancel) {
                Text("Cancel", color = TealPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
