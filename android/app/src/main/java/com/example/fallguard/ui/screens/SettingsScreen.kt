package com.example.fallguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fallguard.data.LocalContact
import com.example.fallguard.ui.theme.*
import com.example.fallguard.viewmodel.HomeViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onReset: () -> Unit = {},
    vmFactory: androidx.lifecycle.ViewModelProvider.Factory? = null
) {
    val vm: HomeViewModel = if (vmFactory != null) viewModel(factory = vmFactory) else viewModel()
    val state by vm.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Profile?", fontWeight = FontWeight.Bold) },
            text = { Text("This will erase your name, emergency contacts, and all settings. The onboarding flow will restart next time you open the app.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetProfile()
                    showResetDialog = false
                    onReset()
                }) {
                    Text("Reset", color = RedAlert, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Profile section
                SectionHeader("PROFILE")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar(state.userName.take(1).uppercase().ifBlank { "?" }, TealPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        state.userName.ifBlank { "Not set" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Emergency contacts
                SectionHeader("EMERGENCY CONTACTS - EMS WILL BE FALL BACK")
                Spacer(modifier = Modifier.height(8.dp))
                if (state.contacts.isEmpty()) {
                    Text(
                        "No contacts added",
                        fontSize = 14.sp,
                        color = TextHint,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    state.contacts.forEach { contact ->
                        SettingsContactRow(contact)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // EMS Mode
                SectionHeader("EMS MODE - CALL EMS INSTEAD OF CONTACTS")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Background, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactAvatar("E", TealPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EMS", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Priority 1", fontSize = 13.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = state.emsModeEnabled,
                        onCheckedChange = null, // read-only in settings; edit in profile setup
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceWhite,
                            checkedTrackColor = TealPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About section
                SectionHeader("ABOUT")
                Spacer(modifier = Modifier.height(8.dp))
                AboutRow("Fall Detection", "Accel + Gyro")
                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                AboutRow("Grace Period", "20 seconds")
                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                AboutRow("Response Timeout", "5 minutes")

                Spacer(modifier = Modifier.height(32.dp))

                // Reset button
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
                ) {
                    Text("Reset Profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun SettingsContactRow(contact: LocalContact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(contact.name.take(1).uppercase(), TealPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(contact.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Priority ${contact.priority}", fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp, color = TextPrimary)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
