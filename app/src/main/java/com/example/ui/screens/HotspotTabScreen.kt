package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.WifiTetheringOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.HotspotState
import com.example.network.QrCodeHelper
import com.example.ui.components.ModernCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HotspotTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customSsid by viewModel.customSsid.collectAsStateWithLifecycle()
    val customPassword by viewModel.customPassword.collectAsStateWithLifecycle()
    val selectedSecurity by viewModel.selectedSecurity.collectAsStateWithLifecycle()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val hotspotState by viewModel.hotspotState.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveProfileTitle by remember { mutableStateOf("") }
    var saveProfileNote by remember { mutableStateOf("") }
    var showHowToDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "কাস্টম হটস্পট শেয়ার",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Custom Name & Password Hotspot",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (val state = hotspotState) {
                is HotspotState.Active -> {
                    StatusPill(
                        text = "হটস্পট সক্রিয় (Active)",
                        color = StatusGreen,
                        isPulsing = true
                    )
                }
                is HotspotState.Starting -> {
                    StatusPill(
                        text = "চালু হচ্ছে...",
                        color = StatusAmber,
                        isPulsing = true
                    )
                }
                is HotspotState.Error -> {
                    StatusPill(
                        text = "ত্রুটি (Error)",
                        color = StatusRed
                    )
                }
                else -> {
                    StatusPill(
                        text = "নিষ্ক্রিয় (Ready)",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Active State Banner if running
        if (hotspotState is HotspotState.Active) {
            val active = hotspotState as HotspotState.Active
            ModernCard(
                modifier = Modifier
                    .border(1.5.dp, StatusGreen, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(StatusGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = "Active",
                            tint = StatusGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "হটস্পট সফলভাবে চালু রয়েছে!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusGreen
                        )
                        Text(
                            text = "নেটওয়ার্ক: ${active.ssid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.stopHotspot() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hotspot_stop_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Icon(imageVector = Icons.Default.WifiTetheringOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("হটস্পট বন্ধ করুন (Stop Hotspot)")
                }
            }
        }

        // Error Banner if failed
        if (hotspotState is HotspotState.Error) {
            val err = hotspotState as HotspotState.Error
            ModernCard(
                modifier = Modifier.border(1.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = StatusRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হটস্পট বিজ্ঞপ্তি",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusRed
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = err.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { viewModel.openSystemTetheringSettings() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সিস্টেম সেটিংসে হটস্পট অন করুন")
                }
            }
        }

        // Custom Configuration Form Card
        ModernCard {
            Text(
                text = "১. কাষ্টম নাম ও পাসওয়ার্ড নির্ধারণ করুন",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configure your custom Hotspot SSID and Password",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SSID Input
            OutlinedTextField(
                value = customSsid,
                onValueChange = { viewModel.setSsid(it) },
                label = { Text("হটস্পটের নাম (Network Name / SSID)") },
                placeholder = { Text("e.g. MyFastHotspot") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "SSID",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ssid_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            OutlinedTextField(
                value = customPassword,
                onValueChange = { viewModel.setPassword(it) },
                label = { Text("কাষ্টম পাসওয়ার্ড (Password - Min 8 chars)") },
                placeholder = { Text("কমপক্ষে ৮ সংখ্যার পাসওয়ার্ড") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.generateRandomPassword()
                                Toast.makeText(context, "স্ট্রং পাসওয়ার্ড তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Generate strong password",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Security Selector Chips
            Text(
                text = "নিরাপত্তা প্রোটোকল (Security):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("WPA2", "WPA3", "OPEN").forEach { sec ->
                    FilterChip(
                        selected = selectedSecurity == sec,
                        onClick = { viewModel.setSecurity(sec) },
                        label = { Text(sec) },
                        leadingIcon = if (selectedSecurity == sec) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Live Generated QR Code Card
        ModernCard(modifier = Modifier.testTag("qr_code_card")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "২. তাৎক্ষণিক স্ক্যান কিউআর কোড",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Instant Connect QR Code",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.regenerateQrCode() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big QR Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Hotspot Wi-Fi QR Code",
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 যেকোনো মোবাইল ফোনের ক্যামেরা দিয়ে এই QR কোডটি স্ক্যান করলেই কোনো পাসওয়ার্ড টাইপ না করেই আপনার হটস্পটে সরাসরি যুক্ত হবে!",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Password
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hotspot Password", customPassword)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "পাসওয়ার্ড কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_password_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কপি পাসওয়ার্ড", fontSize = 12.sp)
                }

                // Share QR
                OutlinedButton(
                    onClick = {
                        qrBitmap?.let { bmp ->
                            QrCodeHelper.shareQrCode(context, bmp, customSsid)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কিউআর শেয়ার", fontSize = 12.sp)
                }

                // Save Profile
                OutlinedButton(
                    onClick = {
                        saveProfileTitle = customSsid
                        showSaveDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সেভ করুন", fontSize = 12.sp)
                }
            }
        }

        // Hotspot Actions
        ModernCard {
            Text(
                text = "৩. হটস্পট কন্ট্রোল ও শেয়ারিং অপশন",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Start Local Hotspot Button
            if (hotspotState !is HotspotState.Active) {
                Button(
                    onClick = { viewModel.startHotspot() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("hotspot_toggle_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (hotspotState is HotspotState.Starting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("হটস্পট তৈরি হচ্ছে...")
                    } else {
                        Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("লোকাল হটস্পট শুরু করুন (Start Hotspot)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct System Hotspot / Wi-Fi Bridge Settings
            Button(
                onClick = { viewModel.openSystemTetheringSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("system_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সিস্টেম হটস্পট সেটিংস (Wi-Fi Bridge)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guidance hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHowToDialog = true }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "কানেক্টেড ওয়াই-ফাই অন্য ডিভাইসে কীভাবে শেয়ার করবেন? জানুন",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("হটস্পট প্রোফাইল সংরক্ষণ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ভবিষ্যতে এক ক্লিকে ব্যবহারের জন্য এই কাস্টম সেটিংস সেভ করে রাখুন:")
                    OutlinedTextField(
                        value = saveProfileTitle,
                        onValueChange = { saveProfileTitle = it },
                        label = { Text("প্রোফাইলের নাম (e.g. বাসায় শেয়ার)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = saveProfileNote,
                        onValueChange = { saveProfileNote = it },
                        label = { Text("নোট (ঐচ্ছিক)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentAsProfile(saveProfileTitle, saveProfileNote)
                        showSaveDialog = false
                        Toast.makeText(context, "প্রোফাইল সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Help Dialog
    if (showHowToDialog) {
        AlertDialog(
            onDismissRequest = { showHowToDialog = false },
            title = { Text("কানেক্টেড নেটওয়ার্ক শেয়ার করার নিয়ম") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("১. আপনার ডিভাইসটি যদি ইতিমধ্যে কোনো ওয়াই-ফাই নেটওয়ার্কের সাথে কানেক্ট থাকে:")
                    Text("• আধুনিক বেশিরভাগ অ্যান্ড্রয়েড ফোনেই 'Wi-Fi Sharing / Wi-Fi Bridge' প্রযুক্তি থাকে। অর্থাৎ ওয়াই-ফাই অন রেখেই আপনি হটস্পট চালু করতে পারবেন।")
                    Text("২. এই অ্যাপের ফর্মটিতে আপনার পছন্দমতো নাম (SSID) এবং পাসওয়ার্ড দিন।")
                    Text("৩. 'সিস্টেম হটস্পট সেটিংস' বাটনে চাপ দিয়ে ফোনের হটস্পট চালু করুন এবং উপরে দেওয়া নাম ও পাসওয়ার্ড দিয়ে দিন।")
                    Text("৪. অপর প্রান্তে থাকা ব্যক্তি শুধু আপনার ফোনের স্ক্রিনে প্রদর্শিত QR কোডটি স্ক্যান করলেই সাথে সাথে কানেক্ট হয়ে যাবে!")
                }
            },
            confirmButton = {
                Button(onClick = { showHowToDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}
