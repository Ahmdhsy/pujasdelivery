package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Menu(
    val id: Int,
    @SerializedName("nama") val name: String,
    @SerializedName("harga") val price: String,
    val deskripsi: String,
    val gambar: String,
    val tenant: String,
    val category: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) {
    fun getPriceAsInt(): Int {
        return try {
            price.replace(".00", "").toInt()
        } catch (e: Exception) {
            0
        }
    }
}

