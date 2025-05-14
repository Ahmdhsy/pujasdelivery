package com.example.pujasdelivery.api

import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.Tenant
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MenuApiService {
    @GET("api/menus")
    fun getMenus(): Call<List<Menu>>

    @GET("api/tenants")
    fun getTenants(): Call<List<Tenant>>

    @POST("api/tenants")
    fun addTenant(@Body tenant: Tenant): Call<Tenant> // Ubah respons sesuai kebutuhan
}