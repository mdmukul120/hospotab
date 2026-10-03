package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.HotspotProfile
import com.example.data.repository.HotspotRepository
import com.example.network.ActiveNetworkDetails
import com.example.network.HotspotManager
import com.example.network.HotspotState
import com.example.network.LocalWebServer
import com.example.network.NetworkInfoHelper
import com.example.network.PortalMessage
import com.example.network.QrCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HotspotRepository
    private val hotspotManager = HotspotManager(application)
    private val networkInfoHelper = NetworkInfoHelper(application, viewModelScope)
    private val localWebServer = LocalWebServer(viewModelScope)

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = HotspotRepository(database.hotspotProfileDao())
        networkInfoHelper.startMonitoring()
    }

    // Input States
    private val _customSsid = MutableStateFlow("NetShare_Hotspot")
    val customSsid: StateFlow<String> = _customSsid.asStateFlow()

    private val _customPassword = MutableStateFlow("sharepass2026")
    val customPassword: StateFlow<String> = _customPassword.asStateFlow()

    private val _selectedSecurity = MutableStateFlow("WPA2")
    val selectedSecurity: StateFlow<String> = _selectedSecurity.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    // Generated QR Code Bitmap
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    // System States
    val hotspotState: StateFlow<HotspotState> = hotspotManager.hotspotState
    val networkState: StateFlow<ActiveNetworkDetails> = networkInfoHelper.networkState

    // Room Database Saved Profiles
    val savedProfiles: StateFlow<List<HotspotProfile>> = repository.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Local Web Server States
    val isServerRunning: StateFlow<Boolean> = localWebServer.isRunning
    val serverPort: StateFlow<Int> = localWebServer.port
    val portalMessages: StateFlow<List<PortalMessage>> = localWebServer.messages
    val hostAnnouncement: StateFlow<String> = localWebServer.hostAnnouncement

    // Ping Diagnostic
    private val _pingResult = MutableStateFlow<Pair<Boolean, Long>?>(null)
    val pingResult: StateFlow<Pair<Boolean, Long>?> = _pingResult.asStateFlow()

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging.asStateFlow()

    init {
        regenerateQrCode()
    }

    fun setSsid(ssid: String) {
        _customSsid.value = ssid
        regenerateQrCode()
    }

    fun setPassword(password: String) {
        _customPassword.value = password
        regenerateQrCode()
    }

    fun setSecurity(security: String) {
        _selectedSecurity.value = security
        regenerateQrCode()
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun generateRandomPassword() {
        val chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        val pass = (1..10).map { chars[random.nextInt(chars.length)] }.joinToString("")
        _customPassword.value = pass
        regenerateQrCode()
    }

    fun regenerateQrCode() {
        viewModelScope.launch(Dispatchers.Default) {
            val qrText = QrCodeHelper.buildWifiConfigString(
                ssid = _customSsid.value,
                password = _customPassword.value,
                securityType = _selectedSecurity.value
            )
            val bitmap = QrCodeHelper.generateQrBitmap(qrText, 600)
            _qrBitmap.value = bitmap
        }
    }

    fun startHotspot() {
        hotspotManager.startLocalHotspot(
            customSsid = _customSsid.value,
            customPass = _customPassword.value,
            securityType = _selectedSecurity.value
        )
    }

    fun stopHotspot() {
        hotspotManager.stopLocalHotspot()
    }

    fun openSystemTetheringSettings() {
        hotspotManager.openSystemTetheringSettings()
    }

    fun openWifiSettings() {
        hotspotManager.openWifiSettings()
    }

    fun refreshNetwork() {
        networkInfoHelper.refreshNetworkInfo()
    }

    fun runPingTest() {
        if (_isPinging.value) return
        _isPinging.value = true
        viewModelScope.launch {
            val result = networkInfoHelper.pingHost("8.8.8.8")
            _pingResult.value = result
            _isPinging.value = false
        }
    }

    fun saveCurrentAsProfile(title: String, note: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = HotspotProfile(
                title = title.ifBlank { _customSsid.value },
                ssid = _customSsid.value,
                password = _customPassword.value,
                securityType = _selectedSecurity.value,
                note = note
            )
            repository.insertProfile(profile)
        }
    }

    fun applyProfile(profile: HotspotProfile) {
        _customSsid.value = profile.ssid
        _customPassword.value = profile.password
        _selectedSecurity.value = profile.securityType
        regenerateQrCode()
    }

    fun deleteProfile(profile: HotspotProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProfile(profile)
        }
    }

    // Local Web Server Control
    fun toggleWebServer() {
        if (isServerRunning.value) {
            localWebServer.stopServer()
        } else {
            localWebServer.startServer(8080)
        }
    }

    fun sendHostAnnouncement(announcement: String) {
        localWebServer.updateAnnouncement(announcement)
    }

    fun sendHostMessage(message: String) {
        localWebServer.addHostMessage(message)
    }

    override fun onCleared() {
        super.onCleared()
        networkInfoHelper.stopMonitoring()
        hotspotManager.stopLocalHotspot()
        localWebServer.stopServer()
    }
}
