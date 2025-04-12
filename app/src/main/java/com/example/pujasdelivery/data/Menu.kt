package com.example.pujasdelivery.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menus",
    foreignKeys = [ForeignKey(
        entity = Tenant::class,
        parentColumns = ["id"],
        childColumns = ["tenantId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["tenantId"])]
)
data class Menu(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tenantId: Int,
    val name: String,
    val price: Int,
    val description: String
)