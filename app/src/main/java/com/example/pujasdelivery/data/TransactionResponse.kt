package com.example.pujasdelivery.data

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: TransactionData
)

data class TransactionData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("gedung_id")
    val gedungId: Int,
    @SerializedName("gedung_name")
    val gedungName: String,
    @SerializedName("tenant_id")
    val tenantId: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("total_price")
    val totalPrice: Double,
    @SerializedName("bukti_pembayaran")
    val buktiPembayaran: String,
    @SerializedName("items")
    val items: List<TransactionItem>
)

data class TransactionItem(
    @SerializedName("transaction_id")
    val transactionId: Int,
    @SerializedName("menu_id")
    val menuId: Int,
    @SerializedName("menu_name")
    val menuName: String,
    @SerializedName("tenant_name")
    val tenantName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double,
    @SerializedName("subtotal")
    val subtotal: Double,
    @SerializedName("catatan")
    val catatan: String?
)