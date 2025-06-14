package com.example.pujasdelivery.api

import com.example.pujasdelivery.data.RegisterRequest
import com.example.pujasdelivery.data.RegisterResponse
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.TransactionStatusRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

@JvmSuppressWildcards
interface ApiService {
    @POST("api/users/register")
    fun registerUser(
        @Header("Authorization") token: String,
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @GET("api/users/me")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<RegisterResponse>

    @Multipart
    @POST("api/transactions")
    fun createTransaction(
        @Header("Authorization") token: String,
        @Part("user_id") userId: RequestBody,
        @Part("tenant_id") tenantId: RequestBody,
        @Part("gedung_id") gedungId: RequestBody,
        @PartMap items: Map<String, RequestBody>,
        @Part buktiPembayaran: MultipartBody.Part
    ): Call<TransactionResponse>

    @GET("api/transactions/{id}")
    fun getTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<TransactionResponse>

    @GET("api/user/transactions")
    fun getUserTransactions(
        @Header("Authorization") token: String,
        @Query("status") status: String
    ): Call<List<TransactionResponse>>

    @GET("api/transactions/courier/ongoing")
    suspend fun getCourierOngoingTransactions(
        @Header("Authorization") token: String
    ): List<TransactionResponse>

    @GET("api/transactions/courier/history")
    suspend fun getCourierHistoryTransactions(
        @Header("Authorization") token: String
    ): List<TransactionResponse>

    @PUT("api/transactions/{id}/status")
    suspend fun updateTransactionStatus(
        @Header("Authorization") token: String,
        @Path("id") transactionId: Int,
        @Body statusRequest: TransactionStatusRequest
    ): TransactionResponse
}