package com.example.fallguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fallguard.ui.theme.*

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TealContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TealPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FallGuard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart fall detection that keeps\nyou and your loved ones safe",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Feature steps
            FeatureStep(number = "1", title = "Detect", subtitle = "Advanced sensors monitor for falls")
            Spacer(modifier = Modifier.height(16.dp))
            FeatureStep(number = "2", title = "Alert", subtitle = "Instantly notifies emergency contacts\nor notifies EMS")
            Spacer(modifier = Modifier.height(16.dp))
            FeatureStep(number = "3", title = "Respond", subtitle = "Your loved ones can respond to quick\ncheck-ins to confirm your safety")

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Your data stays private and secure!",
                fontSize = 13.sp,
                color = TextHint,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureStep(number: String, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
        }
    }
}
