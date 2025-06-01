package com.example.pujasdelivery.api

import com.example.pujasdelivery.data.RegisterRequest
import com.example.pujasdelivery.data.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
}