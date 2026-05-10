package com.example.fallguard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fallguard.ui.theme.*

@Composable
fun ResolvedScreen(onBackHome: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "greenPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing green checkmark
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(GreenSuccessLight)
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(GreenSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Alert Cancelled",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(72.dp))

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Back to home", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
