package com.example.pujasdelivery.api

import com.example.pujasdelivery.data.ApiResponse
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.Tenant
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MenuApiService {
    @GET("api/menus")
    fun getMenus(): Call<ApiResponse<List<Menu>>>

    @GET("api/tenants")
    fun getTenants(): Call<ApiResponse<List<Tenant>>>

    @POST("api/tenants")
    fun addTenant(@Body tenant: Tenant): Call<ApiResponse<Tenant>>
}