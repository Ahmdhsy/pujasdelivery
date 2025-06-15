
package com.example.pujasdelivery.data

import com.google.gson.annotations.SerializedName

data class Order(
    val id: String,
    @SerializedName("tenant_name") val tenantName: String,
    val status: String,
    @SerializedName("total_price") val totalPrice: Int,
    val items: List<CartItem>,
    @SerializedName("proof_image_uri") val proofImageUri: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
