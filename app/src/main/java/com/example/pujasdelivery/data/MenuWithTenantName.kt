package com.example.pujasdelivery.data

import androidx.room.ColumnInfo

data class MenuWithTenantName(
    val id: Int,
    val tenantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    @ColumnInfo(name = "tenantName") // Nama kolom dari hasil JOIN
    val tenantName: String
)