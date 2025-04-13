package com.example.pujasdelivery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.data.AppDatabase
import com.example.pujasdelivery.data.ChatMessage
import com.example.pujasdelivery.websocket.ChatWebSocketClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.net.URI

class ChatViewModel(application: Application, private val userId: Int, private val tenantId: Int) : AndroidViewModel(application) {
    private val chatMessageDao = AppDatabase.getDatabase(application).chatMessageDao()
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> get() = _messages

    private var webSocketClient: ChatWebSocketClient? = null

    init {
        // Komentari WebSocket untuk debugging, aktifkan kembali saat server siap
        // setupWebSocket()
        loadMessages()
    }

    private fun setupWebSocket() {
        // Ganti dengan URL server WebSocket Anda
        val serverUri = URI("ws://your-websocket-server-url") // Ganti dengan URL server kamu
        webSocketClient = ChatWebSocketClient(serverUri) { message ->
            // Tangani pesan yang diterima
            val chatMessage = ChatMessage(
                senderId = tenantId, // Asumsikan pesan dari tenant
                receiverId = userId,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            viewModelScope.launch {
                chatMessageDao.insert(chatMessage)
                loadMessages()
            }
        }
        webSocketClient?.connect()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatMessageDao.getMessagesBetween(userId, tenantId).collect { messageList ->
                _messages.postValue(messageList)
            }
        }
    }

    fun sendMessage(message: String) {
        val chatMessage = ChatMessage(
            senderId = userId,
            receiverId = tenantId,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            chatMessageDao.insert(chatMessage)
            // Komentari WebSocket untuk debugging
            // webSocketClient?.sendMessage(message)
            loadMessages()
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient?.close()
    }
}