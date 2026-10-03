package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.network.QrCodeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NetShare Hotspot", appName)
    }

    @Test
    fun `generate qr bitmap creates valid bitmap`() {
        val bitmap = QrCodeHelper.generateQrBitmap("WIFI:S:Test;T:WPA;P:pass;;", 200)
        assertNotNull(bitmap)
        assertEquals(200, bitmap?.width)
        assertEquals(200, bitmap?.height)
    }
}
