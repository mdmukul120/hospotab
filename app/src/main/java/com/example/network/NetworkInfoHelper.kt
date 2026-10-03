package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

data class ActiveNetworkDetails(
    val isConnected: Boolean = false,
    val isWifi: Boolean = false,
    val isCellular: Boolean = false,
    val ssid: String = "Not Connected",
    val bssid: String = "N/A",
    val ipAddress: String = "0.0.0.0",
    val ipv6Address: String = "N/A",
    val gateway: String = "N/A",
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val bandDescription: String = "Unknown",
    val signalDbm: Int = -100,
    val signalPercent: Int = 0,
    val dnsServers: List<String> = emptyList(),
    val isInternetValidated: Boolean = false
)

class NetworkInfoHelper(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _networkState = MutableStateFlow(ActiveNetworkDetails())
    val networkState: StateFlow<ActiveNetworkDetails> = _networkState.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startMonitoring() {
        refreshNetworkInfo()

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { refreshNetworkInfo() }
            }

            override fun onLost(network: Network) {
                scope.launch { refreshNetworkInfo() }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                scope.launch { refreshNetworkInfo() }
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties
            ) {
                scope.launch { refreshNetworkInfo() }
            }
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        } catch (e: Exception) {
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback!!)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }

    fun refreshNetworkInfo() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork)

        if (activeNetwork == null || capabilities == null) {
            _networkState.value = ActiveNetworkDetails(
                isConnected = false,
                ipAddress = getLocalIpAddress() ?: "Disconnected"
            )
            return
        }

        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        var ssid = "Connected Network"
        var bssid = "N/A"
        var linkSpeed = 0
        var frequency = 0
        var signalDbm = -100
        var signalPercent = 0

        if (isWifi) {
            try {
                val wifiInfo: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    capabilities.transportInfo as? WifiInfo ?: wifiManager.connectionInfo
                } else {
                    wifiManager.connectionInfo
                }

                wifiInfo?.let { info ->
                    val rawSsid = info.ssid
                    ssid = if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>") {
                        rawSsid.removeSurrounding("\"")
                    } else {
                        "Wi-Fi Network (Active)"
                    }
                    bssid = info.bssid ?: "N/A"
                    linkSpeed = info.linkSpeed
                    frequency = info.frequency
                    signalDbm = info.rssi
                    signalPercent = calculateSignalPercent(info.rssi)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (isCellular) {
            ssid = "Mobile Cellular Data"
        }

        val ipAddress = getLocalIpAddress() ?: "0.0.0.0"
        val ipv6 = getLocalIpv6Address() ?: "N/A"

        var gateway = "N/A"
        val dnsList = mutableListOf<String>()

        linkProperties?.let { lp ->
            for (route in lp.routes) {
                if (route.isDefaultRoute && route.gateway != null) {
                    gateway = route.gateway?.hostAddress ?: "N/A"
                    break
                }
            }
            for (dns in lp.dnsServers) {
                dns.hostAddress?.let { dnsList.add(it) }
            }
        }

        val bandDesc = when {
            frequency in 2400..2500 -> "2.4 GHz"
            frequency in 4900..5900 -> "5 GHz"
            frequency > 5900 -> "6 GHz (Wi-Fi 6E)"
            else -> if (isWifi) "Standard Wi-Fi" else "Cellular / LAN"
        }

        _networkState.value = ActiveNetworkDetails(
            isConnected = true,
            isWifi = isWifi,
            isCellular = isCellular,
            ssid = ssid,
            bssid = bssid,
            ipAddress = ipAddress,
            ipv6Address = ipv6,
            gateway = gateway,
            linkSpeedMbps = linkSpeed,
            frequencyMhz = frequency,
            bandDescription = bandDesc,
            signalDbm = signalDbm,
            signalPercent = signalPercent,
            dnsServers = dnsList,
            isInternetValidated = isValidated
        )
    }

    private fun calculateSignalPercent(rssi: Int): Int {
        return when {
            rssi <= -100 -> 0
            rssi >= -50 -> 100
            else -> 2 * (rssi + 100)
        }.coerceIn(0, 100)
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun getLocalIpv6Address(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr !is Inet4Address) {
                        val host = addr.hostAddress ?: ""
                        val delim = host.indexOf('%')
                        return if (delim < 0) host else host.substring(0, delim)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    suspend fun pingHost(host: String = "8.8.8.8"): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(2000)
            val latency = System.currentTimeMillis() - start
            Pair(reachable, latency)
        } catch (e: Exception) {
            Pair(false, -1L)
        }
    }
}
