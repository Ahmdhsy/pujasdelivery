package com.example.pujasdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName

@Dao
interface MenuDao {
    @Insert
    suspend fun insert(menu: Menu): Long

    @Insert
    suspend fun insertAll(vararg menus: Menu): List<Long>

    @Query("SELECT menus.*, tenants.name AS tenantName FROM menus JOIN tenants ON menus.tenantId = tenants.id WHERE menus.tenantId = :tenantId")
    suspend fun getMenusWithTenantName(tenantId: Int): List<MenuWithTenantName>
}