package com.example.pujasdelivery.data

data class ApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: List<T>
)