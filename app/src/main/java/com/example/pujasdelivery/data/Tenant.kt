package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Tenant(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageURL: String? = null,
    val phone: String? = null,
    val status: String? = null
)