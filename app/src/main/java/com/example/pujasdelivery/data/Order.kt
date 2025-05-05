package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val totalPrice: Int,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val deliveryAddress: String,
    val proofImageUri: String? = null,
    val courierId: Int? = null
)