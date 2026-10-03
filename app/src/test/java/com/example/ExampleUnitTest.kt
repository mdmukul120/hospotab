package com.example

import com.example.network.QrCodeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testWifiQrCodeGenerationString() {
        val configStr = QrCodeHelper.buildWifiConfigString(
            ssid = "MyOffice_Share",
            password = "testpassword123",
            securityType = "WPA2"
        )
        assertEquals("WIFI:S:MyOffice_Share;T:WPA;P:testpassword123;;", configStr)
    }

    @Test
    fun testWifiQrCodeOpenNetworkString() {
        val configStr = QrCodeHelper.buildWifiConfigString(
            ssid = "FreeCoffee",
            password = "",
            securityType = "OPEN"
        )
        assertEquals("WIFI:S:FreeCoffee;T:nopass;;", configStr)
    }

    @Test
    fun testWifiQrCodeSpecialCharacterEscaping() {
        val configStr = QrCodeHelper.buildWifiConfigString(
            ssid = "Home;Wifi:1",
            password = "pass;word\\key",
            securityType = "WPA"
        )
        assertTrue(configStr.contains("Home\\;Wifi\\:1"))
        assertTrue(configStr.contains("pass\\;word\\\\key"))
    }
}
