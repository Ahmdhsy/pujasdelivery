package com.example.pujasdelivery.websocket

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ChatWebSocketClient(
    serverUri: URI,
    private val onMessageReceived: (String) -> Unit
) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("WebSocket", "Terhubung ke server")
    }

    override fun onMessage(message: String?) {
        message?.let {
            onMessageReceived(it)
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WebSocket", "Koneksi ditutup: $reason")
    }

    override fun onError(ex: Exception?) {
        Log.e("WebSocket", "Kesalahan: ${ex?.message}")
    }

    fun sendMessage(message: String) {
        try {
            send(message) // Method ini sudah ada di WebSocketClient
        } catch (e: Exception) {
            Log.e("WebSocket", "Gagal mengirim pesan: ${e.message}")
        }
    }
}