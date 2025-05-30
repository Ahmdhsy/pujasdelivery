package com.example.pujasdelivery.data

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData? = null,
    @SerializedName("error") val error: String? = null
)

data class UserData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("firebase_uid") val firebaseUid: String,
    @SerializedName("role") val role: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)