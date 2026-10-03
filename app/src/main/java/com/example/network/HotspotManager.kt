package com.example.network

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class HotspotState {
    object Idle : HotspotState()
    object Starting : HotspotState()
    data class Active(
        val ssid: String,
        val passphrase: String,
        val isSystemAssigned: Boolean = false,
        val startedAt: Long = System.currentTimeMillis()
    ) : HotspotState()
    data class Error(val message: String, val errorCode: Int? = null) : HotspotState()
    object Stopped : HotspotState()
}

class HotspotManager(private val context: Context) {
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _hotspotState = MutableStateFlow<HotspotState>(HotspotState.Idle)
    val hotspotState: StateFlow<HotspotState> = _hotspotState.asStateFlow()

    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

    /**
     * Start local hotspot
     */
    fun startLocalHotspot(
        customSsid: String,
        customPass: String,
        securityType: String = "WPA2"
    ) {
        if (_hotspotState.value is HotspotState.Active || _hotspotState.value is HotspotState.Starting) {
            return
        }

        _hotspotState.value = HotspotState.Starting

        try {
            val callback = object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(res: WifiManager.LocalOnlyHotspotReservation) {
                    super.onStarted(res)
                    reservation = res

                    var activeSsid = customSsid
                    var activePass = customPass
                    var isSystemAssigned = false

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val config = res.softApConfiguration
                            if (config != null) {
                                if (config.ssid != null) {
                                    activeSsid = config.ssid ?: customSsid
                                }
                                if (config.passphrase != null) {
                                    activePass = config.passphrase ?: customPass
                                    isSystemAssigned = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    _hotspotState.value = HotspotState.Active(
                        ssid = activeSsid,
                        passphrase = activePass,
                        isSystemAssigned = isSystemAssigned
                    )
                }

                override fun onStopped() {
                    super.onStopped()
                    reservation = null
                    _hotspotState.value = HotspotState.Stopped
                }

                override fun onFailed(reason: Int) {
                    super.onFailed(reason)
                    reservation = null
                    val reasonDesc = when (reason) {
                        ERROR_NO_CHANNEL -> "কোনো উপযুক্ত ওয়াই-ফাই চ্যানেল পাওয়া যায়নি (No channel available)"
                        ERROR_GENERIC -> "ডিভাইসে হটস্পট চালু করতে সমস্যা হয়েছে (Generic hotspot error)"
                        ERROR_INCOMPATIBLE_MODE -> "ওয়াই-ফাই মোড অসঙ্গতিপূর্ণ (Incompatible Wi-Fi mode)"
                        ERROR_TETHERING_DISALLOWED -> "সিস্টেম দ্বারা হটস্পট টেথারিং অনুমোদিত নয় (Tethering disallowed)"
                        else -> "হটস্পট শুরু করতে ব্যর্থ হয়েছে (কোড: $reason)"
                    }
                    _hotspotState.value = HotspotState.Error(
                        message = reasonDesc,
                        errorCode = reason
                    )
                }
            }

            wifiManager.startLocalOnlyHotspot(callback, Handler(Looper.getMainLooper()))
        } catch (e: SecurityException) {
            _hotspotState.value = HotspotState.Error(
                message = "হটস্পট চালুর জন্য লোকেশন বা নিয়ারবাই পারমিশন প্রয়োজন (Permission missing: ${e.localizedMessage})"
            )
        } catch (e: IllegalStateException) {
            _hotspotState.value = HotspotState.Error(
                message = "ওয়াই-ফাই বা টেথারিং বর্তমান অবস্থায় চালু করা সম্ভব নয় (System state error: ${e.localizedMessage})"
            )
        } catch (e: Exception) {
            _hotspotState.value = HotspotState.Error(
                message = "ত্রুটি ঘটেছে: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Stop local hotspot
     */
    fun stopLocalHotspot() {
        try {
            reservation?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reservation = null
            _hotspotState.value = HotspotState.Stopped
        }
    }

    /**
     * Direct launch to Android System Native Tethering & Portable Hotspot settings
     */
    fun openSystemTetheringSettings() {
        val intentsToTry = listOf(
            Intent().setClassName("com.android.settings", "com.android.settings.TetherSettings"),
            Intent("android.settings.TETHER_SETTINGS"),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_WIFI_SETTINGS)
        )

        for (intent in intentsToTry) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Try next fallback
            }
        }
    }

    /**
     * Direct launch to Android Wi-Fi settings
     */
    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
