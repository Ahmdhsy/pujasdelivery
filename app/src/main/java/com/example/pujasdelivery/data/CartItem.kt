package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    val menuId: Int,
    val name: String,
    val price: Int,
    val quantity: Int,
    val tenantId: Long, // Tambahkan tenantId
    val tenantName: String // Tambahkan tenantName
)