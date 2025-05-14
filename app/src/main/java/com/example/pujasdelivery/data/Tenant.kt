package com.example.pujasdelivery.data

import com.google.gson.annotations.SerializedName

data class Tenant(
    val id: Int,
    val name: String,
    val phone: String?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)