package com.example.pujasdelivery.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TenantSeeder(private val tenantRepository: TenantRepository) {

    // Menambahkan beberapa data tenant untuk pertama kali
    suspend fun seed() {
        val tenants = listOf(
            Tenant(
                name = "Warung Makan Sederhana",
                description = "Menyediakan masakan rumahan yang lezat dan terjangkau.",
                imageURL = "https://example.com/images/warung_sederhana.jpg"
            ),
            Tenant(
                name = "Kopi Pagi",
                description = "Spesialis kopi lokal dengan cita rasa khas Indonesia.",
                imageURL = "https://example.com/images/kopi_pagi.jpg"
            )
        )
        withContext(Dispatchers.IO) {
            tenants.forEach {
                tenantRepository.addTenant(it)
            }
        }
    }
}
