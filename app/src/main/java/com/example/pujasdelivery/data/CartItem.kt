package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val menuId: Int,
    val menuName: String,
    val tenantId: Int, // Add tenantId field
    val tenantName: String?,
    val price: Int,
    val quantity: Int
)