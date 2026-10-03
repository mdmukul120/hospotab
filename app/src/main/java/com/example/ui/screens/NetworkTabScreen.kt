package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ModernCard
import com.example.ui.components.StatRow
import com.example.ui.components.StatusPill
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NetworkTabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val pingResult by viewModel.pingResult.collectAsStateWithLifecycle()
    val isPinging by viewModel.isPinging.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "কানেক্টেড নেটওয়ার্ক বিবরণ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Active Network Inspection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { viewModel.refreshNetwork() },
                modifier = Modifier.testTag("refresh_network_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Network")
            }
        }

        // Active Connection Hero Card
        ModernCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            if (networkState.isConnected) StatusGreen.copy(alpha = 0.15f)
                            else StatusRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Connection",
                        tint = if (networkState.isConnected) StatusGreen else StatusRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = networkState.ssid,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (networkState.isConnected) "IP: ${networkState.ipAddress}" else "কোনো নেটওয়ার্কে যুক্ত নেই",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(
                    text = if (networkState.isConnected) "কানেক্টেড" else "অফলাইন",
                    color = if (networkState.isConnected) StatusGreen else StatusRed,
                    isPulsing = networkState.isConnected
                )
            }

            if (networkState.isWifi) {
                Spacer(modifier = Modifier.height(14.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "সিগন্যাল শক্তি (Signal Quality)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${networkState.signalPercent}% (${networkState.signalDbm} dBm)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { networkState.signalPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }

        // Detailed Network Specs Card
        ModernCard {
            Text(
                text = "নেটওয়ার্ক স্পেসিফিকেশন (Specifications)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            StatRow(
                icon = Icons.Default.Speed,
                label = "লিঙ্ক স্পিড (Link Speed)",
                value = if (networkState.linkSpeedMbps > 0) "${networkState.linkSpeedMbps} Mbps" else "N/A"
            )
            StatRow(
                icon = Icons.Default.Wifi,
                label = "ফ্রিকোয়েন্সি ও ব্যান্ড (Band)",
                value = networkState.bandDescription + if (networkState.frequencyMhz > 0) " (${networkState.frequencyMhz} MHz)" else ""
            )
            StatRow(
                icon = Icons.Default.Router,
                label = "গেটওয়ে আইপি (Gateway IP)",
                value = networkState.gateway
            )
            StatRow(
                icon = Icons.Default.Dns,
                label = "ডিএনএস সার্ভার (DNS Servers)",
                value = if (networkState.dnsServers.isNotEmpty()) networkState.dnsServers.joinToString(", ") else "Automatic"
            )
            StatRow(
                icon = Icons.Default.NetworkCheck,
                label = "IPv6 অ্যাড্রেস",
                value = networkState.ipv6Address
            )
        }

        // Ping Diagnostics Tool Card
        ModernCard {
            Text(
                text = "ইন্টারনেট ল্যাটেন্সি টেস্ট (Ping Test)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Test your network reachability and packet delay",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.runPingTest() },
                    enabled = !isPinging,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("ping_test_button")
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("টেস্ট চলছে...")
                    } else {
                        Icon(imageVector = Icons.Default.NetworkPing, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পিং টেস্ট (Ping 8.8.8.8)")
                    }
                }

                pingResult?.let { res ->
                    val (success, latency) = res
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (success) StatusGreen else StatusRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (success) "$latency ms" else "ব্যর্থ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (success) StatusGreen else StatusRed
                            )
                        )
                    }
                }
            }
        }

        // Quick Setting Shortcut
        OutlinedButton(
            onClick = { viewModel.openWifiSettings() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ফোনের ওয়াই-ফাই সেটিংস খুলুন (System Wi-Fi Settings)")
        }
    }
}
