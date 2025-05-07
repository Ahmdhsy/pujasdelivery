package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "menus",
    foreignKeys = [ForeignKey(
        entity = Tenant::class,
        parentColumns = ["id"],
        childColumns = ["tenantId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["tenantId"])]
)
data class Menu(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("tenant_id")
    val tenantId: Int,
    @SerializedName("nama")
    val name: String,
    @SerializedName("harga")
    val price: String, // Sementara ubah ke String untuk parsing
    @SerializedName("deskripsi")
    val description: String,
    val category: String
) {
    fun getPriceAsInt(): Int {
        return try {
            price.toFloat().toInt()
        } catch (e: Exception) {
            0
        }
    }
}