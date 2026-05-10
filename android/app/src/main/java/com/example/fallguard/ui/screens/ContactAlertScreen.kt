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

// Screen shown to an emergency contact when they receive an alert
@Composable
fun ContactAlertScreen(
    userName: String = "Someone",
    onRespond: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orangePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
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

            Box(
                modifier = Modifier
                    .background(RedAlertLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("ALERT RECEIVED", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = RedAlert, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "$userName\nmay have fallen!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "They need your help. Please respond\nso we know you're on your way.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pulsing orange indicator
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(OrangeAlert.copy(alpha = 0.20f))
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(OrangeAlert.copy(alpha = 0.40f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(OrangeAlert)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onRespond,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("I'm on my way!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Can't respond right now", color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}