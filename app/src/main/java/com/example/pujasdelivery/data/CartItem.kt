package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val menuId: Long, // Pastikan bertipe Long
    val menuName: String,
    val tenantId: Long?, // Pastikan bertipe Long (nullable jika boleh null)
    val tenantName: String,
    val price: Int,
    val quantity: Int
)