package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HotspotProfile
import com.example.ui.components.ModernCard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfilesTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onProfileApplied: () -> Unit = {}
) {
    val context = LocalContext.current
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSsid by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "সংরক্ষিত হটস্পট প্রোফাইল",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "১-ক্লিকে লোড করুন আপনার প্রিয় হটস্পট কনফিগারেশন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (savedProfiles.isEmpty()) {
                item {
                    ModernCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কোনো প্রোফাইল সংরক্ষিত নেই",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নিচের (+) বাটনে চাপ দিয়ে একটি কাস্টম হটস্পট তৈরি করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(savedProfiles, key = { it.id }) { profile ->
                    ProfileCardItem(
                        profile = profile,
                        onApply = {
                            viewModel.applyProfile(profile)
                            Toast.makeText(context, "'${profile.title}' প্রয়োগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            onProfileApplied()
                        },
                        onDelete = {
                            viewModel.deleteProfile(profile)
                            Toast.makeText(context, "প্রোফাইল মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
            }
        }

        // FAB to add new profile
        FloatingActionButton(
            onClick = {
                newTitle = ""
                newSsid = ""
                newPassword = ""
                newNote = ""
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_profile_fab"),
            containerColor = CyanPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile")
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন কাস্টম হটস্পট প্রোফাইল") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("প্রোফাইলের নাম (e.g. অফিস নেট)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSsid,
                        onValueChange = { newSsid = it },
                        label = { Text("হটস্পট নাম (SSID)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("পাসওয়ার্ড (কমপক্ষে ৮ ডিজিট)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        label = { Text("বিবরণ / নোট (ঐচ্ছিক)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSsid.isNotBlank() && newPassword.length >= 8) {
                            viewModel.saveCurrentAsProfile(
                                title = newTitle.ifBlank { newSsid },
                                note = newNote
                            )
                            showAddDialog = false
                            Toast.makeText(context, "প্রোফাইল সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "দয়া করে সঠিক নাম ও কমপক্ষে ৮ সংখ্যার পাসওয়ার্ড দিন", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun ProfileCardItem(
    profile: HotspotProfile,
    onApply: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (profile.note.isNotBlank()) {
                        Text(
                            text = profile.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Profile",
                        tint = StatusRed.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SSID: ${profile.ssid}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = profile.password,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_profile_${profile.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("এই হটস্পট সক্রিয় করুন (Apply & Generate QR)")
            }
        }
    }
}
