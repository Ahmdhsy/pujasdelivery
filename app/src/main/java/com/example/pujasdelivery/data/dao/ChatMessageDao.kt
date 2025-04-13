package com.example.pujasdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pujasdelivery.data.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId AND receiverId = :tenantId) OR (senderId = :tenantId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessagesBetween(userId: Int, tenantId: Int): Flow<List<ChatMessage>>
}