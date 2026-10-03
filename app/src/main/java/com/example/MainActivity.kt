package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.HotspotTabScreen
import com.example.ui.screens.NetworkTabScreen
import com.example.ui.screens.ProfilesTabScreen
import com.example.ui.screens.WebShareTabScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Check permissions
    var hasPermissions by remember {
        mutableStateOf(checkRequiredPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermissions = checkRequiredPermissions(context)
        if (hasPermissions) {
            viewModel.refreshNetwork()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Hotspot
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.WifiTethering else Icons.Outlined.WifiTethering,
                            contentDescription = "Hotspot"
                        )
                    },
                    label = { Text(stringResource(R.string.tab_hotspot), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_hotspot"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = CyanPrimary
                    )
                )

                // Tab 1: Network
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Wifi else Icons.Outlined.Wifi,
                            contentDescription = "Network"
                        )
                    },
                    label = { Text(stringResource(R.string.tab_network), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_network"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = CyanPrimary
                    )
                )

                // Tab 2: Profiles
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Profiles"
                        )
                    },
                    label = { Text(stringResource(R.string.tab_profiles), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_profiles"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = CyanPrimary
                    )
                )

                // Tab 3: Web Share
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Language else Icons.Outlined.Language,
                            contentDescription = "Web Share"
                        )
                    },
                    label = { Text(stringResource(R.string.tab_sharing), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_webshare"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = CyanPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        androidx.activity.compose.BackHandler(enabled = selectedTab != 0) {
            selectedTab = 0
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "tabTransition") { tab ->
                when (tab) {
                    0 -> HotspotTabScreen(viewModel = viewModel)
                    1 -> NetworkTabScreen(viewModel = viewModel)
                    2 -> ProfilesTabScreen(
                        viewModel = viewModel,
                        onProfileApplied = { selectedTab = 0 }
                    )
                    3 -> WebShareTabScreen(viewModel = viewModel)
                }
            }
        }
    }
}

private fun checkRequiredPermissions(context: android.content.Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val nearbyWifi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    return fineLocation && nearbyWifi
}
