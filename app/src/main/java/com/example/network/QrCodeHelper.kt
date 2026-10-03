package com.example.network

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap

object QrCodeHelper {

    /**
     * Builds standard Wi-Fi QR Code string representation:
     * WIFI:S:<SSID>;T:<SECURITY>;P:<PASSWORD>;H:<HIDDEN>;;
     */
    fun buildWifiConfigString(
        ssid: String,
        password: String,
        securityType: String = "WPA",
        isHidden: Boolean = false
    ): String {
        val escapedSsid = escapeSpecialChars(ssid)
        val escapedPass = escapeSpecialChars(password)
        val sec = when (securityType.uppercase()) {
            "OPEN", "NONE" -> "nopass"
            "WPA3", "SAE" -> "SAE"
            else -> "WPA" // WPA/WPA2-PSK
        }
        val hiddenStr = if (isHidden) "H:true;" else ""
        return if (sec == "nopass") {
            "WIFI:S:$escapedSsid;T:nopass;${hiddenStr};"
        } else {
            "WIFI:S:$escapedSsid;T:$sec;P:$escapedPass;${hiddenStr};"
        }
    }

    private fun escapeSpecialChars(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(":", "\\:")
            .replace(",", "\\,")
            .replace("\"", "\\\"")
    }

    /**
     * Encodes a string into an Android Bitmap using ZXing
     */
    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            }
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            val colorDark = 0xFF0B131E.toInt() // Dark obsidian
            val colorLight = 0xFFFFFFFF.toInt() // Crisp white

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) colorDark else colorLight
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves QR code bitmap to cache and opens system share sheet
     */
    fun shareQrCode(context: Context, bitmap: Bitmap, ssid: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "hotspot_qr_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Connect to Wi-Fi Hotspot: $ssid")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Hotspot QR Code"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
