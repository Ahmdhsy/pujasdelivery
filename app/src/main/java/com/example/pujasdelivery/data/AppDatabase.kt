package com.example.pujasdelivery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pujasdelivery.data.dao.ChatMessageDao
import com.example.pujasdelivery.data.dao.TenantDao
import com.example.pujasdelivery.data.dao.MenuDao

@Database(entities = [Tenant::class, Menu::class, ChatMessage::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun menuDao(): MenuDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pujas_database"
                )
                    .fallbackToDestructiveMigration() // Tambahkan ini untuk debugging
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}