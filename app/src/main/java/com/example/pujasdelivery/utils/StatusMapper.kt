package com.example.pujasdelivery.utils

object StatusMapper {
    fun mapStatusToDisplay(status: String): String {
        return when (status) {
            "pending" -> "Pending"
            "diproses" -> "Diproses"
            "pengantaran" -> "Dalam Pengantaran"
            "selesai" -> "Selesai"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }
}