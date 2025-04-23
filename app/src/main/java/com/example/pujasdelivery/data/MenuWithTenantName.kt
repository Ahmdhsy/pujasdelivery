package com.example.pujasdelivery.data

import androidx.room.ColumnInfo

/**
 * Data class representing a menu item with its associated tenant name.
 * This is typically used to hold the result of a Room query that joins the Menu and Tenant tables.
 */
data class MenuWithTenantName(
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "tenantId")
    val tenantId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "price")
    val price: Int,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "category") // Add category field
    val category: String,
    @ColumnInfo(name = "tenantName") // Name of the column from the JOIN result
    val tenantName: String?
)