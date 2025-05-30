package com.example.pujasdelivery.data

data class RegisterRequest(
    val firebase_uid: String,
    val email: String,
    val name: String
)