package com.example.pujasdelivery.data

data class CartItem(
    val menuId: Int,
    val name: String,
    val price: Int,
    val quantity: Int,
    val tenantId: Long,
    val tenantName: String,
    val catatan: String? = null // Catatan pengguna untuk item, null jika tidak ada
)