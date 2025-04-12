package com.example.pujasdelivery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pujasdelivery.data.dao.TenantDao
import com.example.pujasdelivery.data.dao.MenuDao

@Database(entities = [Tenant::class, Menu::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun menuDao(): MenuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pujas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}