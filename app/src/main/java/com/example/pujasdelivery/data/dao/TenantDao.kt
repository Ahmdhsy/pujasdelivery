package com.example.pujasdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.lifecycle.LiveData
import com.example.pujasdelivery.data.Tenant

@Dao
interface TenantDao {
    @Insert
    suspend fun insert(tenant: Tenant): Long

    @Insert
    suspend fun insertAll(vararg tenants: Tenant): List<Long>

    @Query("SELECT * FROM tenants")
    suspend fun getAllTenants(): List<Tenant>

    @Query("SELECT * FROM tenants")
    fun getAllTenantsLiveData(): LiveData<List<Tenant>>

    @Query("DELETE FROM tenants")
    suspend fun deleteAll()
}