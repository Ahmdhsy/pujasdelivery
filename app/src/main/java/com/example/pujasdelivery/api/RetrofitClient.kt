package com.example.pujasdelivery.api

import android.util.Log
import com.example.pujasdelivery.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = getFreshToken()
        val modifiedRequest = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept-Encoding", "gzip") // Aktifkan kompresi GZIP
                .build()
        } else {
            original
        }
        chain.proceed(modifiedRequest)
    }

    private fun getFreshToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return runBlocking {
            try {
                val result = user?.getIdToken(true)?.await()
                val token = result?.token
                MyApplication.token = token
                Log.d("RetrofitClient", "Fresh token obtained")
                token
            } catch (e: Exception) {
                Log.e("RetrofitClient", "Failed to get fresh token: ${e.message}")
                MyApplication.token
            }
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    val menuApiService: MenuApiService by lazy {
        retrofit.create(MenuApiService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}