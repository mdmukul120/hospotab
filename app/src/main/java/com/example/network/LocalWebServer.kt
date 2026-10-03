package com.example.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PortalMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

class LocalWebServer(private val scope: CoroutineScope) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _port = MutableStateFlow(8080)
    val port: StateFlow<Int> = _port.asStateFlow()

    private val _messages = MutableStateFlow<List<PortalMessage>>(
        listOf(
            PortalMessage(
                sender = "Host (ডিভাইস)",
                text = "স্বাগতম! আপনি NetShare লোকাল নেটওয়ার্কে যুক্ত হয়েছেন।"
            )
        )
    )
    val messages: StateFlow<List<PortalMessage>> = _messages.asStateFlow()

    private val _hostAnnouncement = MutableStateFlow("Welcome to NetShare Local Portal!")
    val hostAnnouncement: StateFlow<String> = _hostAnnouncement.asStateFlow()

    fun updateAnnouncement(newAnnouncement: String) {
        _hostAnnouncement.value = newAnnouncement
    }

    fun addHostMessage(text: String) {
        if (text.isBlank()) return
        val current = _messages.value.toMutableList()
        current.add(0, PortalMessage(sender = "Host Phone", text = text))
        _messages.value = current
    }

    fun startServer(port: Int = 8080) {
        if (_isRunning.value) return
        _port.value = port

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                _isRunning.value = true
                Log.d("LocalWebServer", "Server started on port $port")

                while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                    try {
                        val clientSocket = serverSocket!!.accept()
                        launch(Dispatchers.IO) {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!isActive) break
                    }
                }
            } catch (e: Exception) {
                Log.e("LocalWebServer", "Failed to start server", e)
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
            _isRunning.value = false
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val out = PrintWriter(socket.getOutputStream(), true)

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            val method = if (parts.isNotEmpty()) parts[0] else "GET"
            val path = if (parts.size > 1) parts[1] else "/"

            // Parse headers
            var line: String?
            var contentLength = 0
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) break
                if (line!!.lowercase(Locale.ENGLISH).startsWith("content-length:")) {
                    contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Handle POST /send
            if (method.equals("POST", ignoreCase = true) && path.startsWith("/send")) {
                val body = CharArray(contentLength)
                reader.read(body, 0, contentLength)
                val bodyStr = String(body)
                val params = parseFormData(bodyStr)
                val userMsg = params["msg"]?.let { URLDecoder.decode(it, "UTF-8") }
                val userName = params["sender"]?.let { URLDecoder.decode(it, "UTF-8") } ?: "Connected Guest"

                if (!userMsg.isNullOrBlank()) {
                    val current = _messages.value.toMutableList()
                    current.add(0, PortalMessage(sender = userName, text = userMsg))
                    _messages.value = current
                }

                // Redirect back to /
                out.println("HTTP/1.1 303 See Other")
                out.println("Location: /")
                out.println("Connection: close")
                out.println()
                out.flush()
                return
            }

            // Return HTML Page
            val html = buildHtmlPage()
            val bytes = html.toByteArray(Charsets.UTF_8)

            out.println("HTTP/1.1 200 OK")
            out.println("Content-Type: text/html; charset=UTF-8")
            out.println("Content-Length: ${bytes.size}")
            out.println("Connection: close")
            out.println()
            socket.getOutputStream().write(bytes)
            socket.getOutputStream().flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun parseFormData(body: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (pair in body.split("&")) {
            val kv = pair.split("=")
            if (kv.size == 2) {
                map[kv[0]] = kv[1]
            }
        }
        return map
    }

    private fun buildHtmlPage(): String {
        val currentAnnouncement = _hostAnnouncement.value
        val messagesHtml = _messages.value.joinToString("\n") { msg ->
            """
            <div style="background:#1e293b;border-radius:10px;padding:12px 16px;margin-bottom:10px;border-left:4px solid #00ADB5;">
                <div style="display:flex;justify-content:space-between;font-size:12px;color:#94a3b8;margin-bottom:4px;">
                    <span style="font-weight:bold;color:#38bdf8;">${escapeHtml(msg.sender)}</span>
                    <span>${msg.timestamp}</span>
                </div>
                <div style="font-size:15px;color:#f8fafc;word-break:break-word;">
                    ${escapeHtml(msg.text)}
                </div>
            </div>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>NetShare Hotspot Portal</title>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    background: #0b131e;
                    color: #f1f5f9;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                }
                .header {
                    text-align: center;
                    padding: 20px;
                    background: linear-gradient(135deg, #00ADB5 0%, #00818A 100%);
                    border-radius: 16px;
                    box-shadow: 0 8px 24px rgba(0, 173, 181, 0.25);
                    margin-bottom: 24px;
                }
                .header h1 { margin: 0 0 6px 0; font-size: 24px; }
                .header p { margin: 0; font-size: 14px; opacity: 0.9; }
                .card {
                    background: #131f2e;
                    border: 1px solid #1e293b;
                    border-radius: 14px;
                    padding: 20px;
                    margin-bottom: 20px;
                }
                .card-title {
                    font-size: 17px;
                    font-weight: 600;
                    margin-top: 0;
                    margin-bottom: 12px;
                    color: #00ADB5;
                }
                input, textarea, button {
                    box-sizing: border-box;
                    width: 100%;
                    border-radius: 8px;
                    font-size: 15px;
                }
                input, textarea {
                    background: #1e293b;
                    border: 1px solid #334155;
                    color: #f8fafc;
                    padding: 12px;
                    margin-bottom: 12px;
                }
                input:focus, textarea:focus {
                    outline: none;
                    border-color: #00ADB5;
                }
                button {
                    background: #00ADB5;
                    border: none;
                    color: white;
                    padding: 12px;
                    font-weight: bold;
                    cursor: pointer;
                    transition: 0.2s;
                }
                button:hover { background: #00818A; }
                .badge {
                    display: inline-block;
                    background: rgba(16, 185, 129, 0.2);
                    color: #10B981;
                    padding: 4px 10px;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: 600;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>📶 NetShare Portal</h1>
                    <p>Connected to Local Hotspot Host</p>
                </div>

                <div class="card">
                    <div style="display:flex;justify-content:space-between;align-items:center;">
                        <span class="card-title" style="margin:0;">📢 Host Announcement</span>
                        <span class="badge">Online</span>
                    </div>
                    <p style="margin-top:12px;margin-bottom:0;color:#cbd5e1;font-size:15px;line-height:1.5;">
                        ${escapeHtml(currentAnnouncement)}
                    </p>
                </div>

                <div class="card">
                    <h3 class="card-title">💬 Send Message to Host</h3>
                    <form action="/send" method="POST">
                        <input type="text" name="sender" placeholder="Your Device Name / Name (e.g. Mukul)" required />
                        <textarea name="msg" rows="3" placeholder="Type your note, message, or link here..." required></textarea>
                        <button type="submit">Send to Host Phone</button>
                    </form>
                </div>

                <div class="card">
                    <h3 class="card-title">📜 Shared Board (${_messages.value.size})</h3>
                    $messagesHtml
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
