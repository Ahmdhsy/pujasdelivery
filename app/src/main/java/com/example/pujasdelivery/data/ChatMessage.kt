package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderId: Int, // ID pengirim (pengguna atau tenant)
    val receiverId: Int, // ID penerima (pengguna atau tenant)
    val message: String,
    val timestamp: Long // Simpan waktu dalam milidetik
)