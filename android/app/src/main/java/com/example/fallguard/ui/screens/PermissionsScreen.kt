package com.example.fallguard.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fallguard.ui.theme.*

@Composable
fun PermissionsScreen(onContinue: () -> Unit) {
    val permissions = buildList {
        add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onContinue() }

    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "App Permissions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FallGuard needs a few permissions\nto keep you safe",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            PermissionRow(
                title = "Motion Sensors",
                subtitle = "Accelerometer & gyroscope to detect falls"
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionRow(
                title = "Activity Recognition (Sensors)",
                subtitle = "Understand movement to reduce false alarms"
            )
            Spacer(modifier = Modifier.height(16.dp))
            PermissionRow(
                title = "Notifications",
                subtitle = "Send alerts to you and your contacts"
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { launcher.launch(permissions.toTypedArray()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Give Permissions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onContinue) {
                Text(
                    "Skip for now",
                    color = TealPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(OrangeAlert)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
