package com.example.pujasdelivery.data

data class MenuWithTenantName(
    val id: Int,
    val tenantId: Long,
    val name: String,
    val price: Int,
    val description: String,
    val category: String,
    val tenantName: String
)