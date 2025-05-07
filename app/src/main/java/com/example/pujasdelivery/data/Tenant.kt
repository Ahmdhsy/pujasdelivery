package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    @SerializedName("imageURL")
    val imageURL: String = "",
    val phone: String? = null,
    val status: String? = null
)