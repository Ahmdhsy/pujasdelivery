package com.example.pujasdelivery.api

import com.example.pujasdelivery.data.ApiResponse
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.Tenant
import retrofit2.Call
import retrofit2.http.GET

interface MenuApiService {
    @GET("api/menus")
    fun getMenus(): Call<ApiResponse<Menu>>

    @GET("api/tenants")
    fun getTenants(): Call<List<Tenant>>
}