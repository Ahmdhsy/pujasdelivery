package com.example.pujasdelivery.ui.screens

data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

data class ChatMessage(
    val message: String,
    val isSentByUser: Boolean
)