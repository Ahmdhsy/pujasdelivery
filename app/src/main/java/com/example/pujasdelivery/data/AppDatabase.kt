package com.example.pujasdelivery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pujasdelivery.data.dao.TenantDao
import com.example.pujasdelivery.data.dao.MenuDao

@Database(entities = [Tenant::class, Menu::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun menuDao(): MenuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE menus ADD COLUMN category TEXT NOT NULL DEFAULT 'Makanan'")
                database.execSQL("UPDATE menus SET category = 'Minuman' WHERE name = 'Es Teh'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pujas_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearDatabase(context: Context) {
            INSTANCE?.let {
                it.clearAllTables()
                INSTANCE = null
            }
        }
    }
}