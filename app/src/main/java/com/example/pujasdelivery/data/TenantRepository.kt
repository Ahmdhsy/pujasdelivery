package com.example.pujasdelivery.data

import com.example.pujasdelivery.data.dao.TenantDao

class TenantRepository(private val tenantDao: TenantDao) {

    // Mendapatkan semua tenant
    suspend fun getAllTenants(): List<Tenant> {
        return tenantDao.getAllTenants()
    }

    // Menambahkan tenant baru
    suspend fun addTenant(tenant: Tenant) {
        tenantDao.insert(tenant)
    }
}
