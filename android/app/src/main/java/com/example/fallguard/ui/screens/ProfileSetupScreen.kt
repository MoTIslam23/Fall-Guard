package com.example.fallguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fallguard.data.LocalContact
import com.example.fallguard.ui.theme.*
import com.example.fallguard.viewmodel.OnboardingViewModel

@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    vmFactory: androidx.lifecycle.ViewModelProvider.Factory? = null
) {
    val vm: OnboardingViewModel = if (vmFactory != null) viewModel(factory = vmFactory) else viewModel()
    val setupComplete by vm.setupComplete.collectAsState()

    var name by remember { mutableStateOf("") }
    var emsMode by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf(listOf<LocalContact>()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(setupComplete) {
        if (setupComplete) onComplete()
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { contactName, phone ->
                val newContact = LocalContact(
                    id = "contact-${System.currentTimeMillis()}",
                    name = contactName,
                    phone = phone,
                    priority = contacts.size + 1
                )
                contacts = contacts + newContact
                showAddDialog = false
            }
        )
    }

    FallGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceWhite)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Set Up Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your name and emergency contacts\nwho will be notified if a fall is detected",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Name field
            SectionLabel("YOUR NAME")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your name", color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Divider
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // EMS Mode
            SectionLabel("EMS MODE - CALL EMS INSTEAD OF CONTACTS")
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
                    Text("Priority 2", fontSize = 13.sp, color = TextSecondary)
                }
                Switch(
                    checked = emsMode,
                    onCheckedChange = { emsMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = TealPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Contacts
            SectionLabel("EMERGENCY CONTACTS - EMS WILL BE FALLBACK")
            Spacer(modifier = Modifier.height(8.dp))

            contacts.forEach { contact ->
                ContactRow(contact = contact, onDelete = {
                    contacts = contacts.filter { it.id != contact.id }
                        .mapIndexed { i, c -> c.copy(priority = i + 1) }
                })
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TealPrimary)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Emergency Contact", color = TealPrimary, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { vm.saveProfile(name, emsMode, contacts) },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Finish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ContactRow(contact: LocalContact, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(contact.name.take(1).uppercase(), TealPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Priority ${contact.priority}", fontSize = 13.sp, color = TextSecondary)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextHint)
        }
    }
}

@Composable
fun ContactAvatar(letter: String, bgColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = bgColor
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AddContactDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var contactName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (contactName.isNotBlank()) onAdd(contactName, phone) },
                enabled = contactName.isNotBlank()
            ) {
                Text("Add", color = TealPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
