package com.example.pujasdelivery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pujasdelivery.data.dao.CartDao
import com.example.pujasdelivery.data.dao.MenuDao
import com.example.pujasdelivery.data.dao.OrderDao
import com.example.pujasdelivery.data.dao.TenantDao

@Database(entities = [Tenant::class, Menu::class, CartItem::class, Order::class, OrderItem::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun menuDao(): MenuDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao // Tambahkan DAO baru

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE menus ADD COLUMN category TEXT NOT NULL DEFAULT 'Makanan'")
                database.execSQL("UPDATE menus SET category = 'Minuman' WHERE name = 'Es Teh'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cart_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        menuId INTEGER NOT NULL,
                        menuName TEXT NOT NULL,
                        tenantName TEXT,
                        price INTEGER NOT NULL,
                        quantity INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cart_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        menuId INTEGER NOT NULL,
                        menuName TEXT NOT NULL,
                        tenantId INTEGER NOT NULL,
                        tenantName TEXT,
                        price INTEGER NOT NULL,
                        quantity INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO cart_items_new (id, menuId, menuName, tenantName, price, quantity, tenantId)
                    SELECT id, menuId, menuName, tenantName, price, quantity, 0
                    FROM cart_items
                """.trimIndent())
                database.execSQL("DROP TABLE cart_items")
                database.execSQL("ALTER TABLE cart_items_new RENAME TO cart_items")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        totalPrice INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        deliveryAddress TEXT NOT NULL,
                        proofImageUri TEXT,
                        courierId INTEGER
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        orderId INTEGER NOT NULL,
                        menuId INTEGER NOT NULL,
                        menuName TEXT NOT NULL,
                        tenantId INTEGER NOT NULL,
                        tenantName TEXT,
                        price INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        FOREIGN KEY (orderId) REFERENCES orders(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_order_items_orderId ON order_items(orderId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pujas_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Tambahkan migrasi baru
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