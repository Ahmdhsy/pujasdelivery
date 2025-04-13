package com.example.pujasdelivery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.data.TenantRepository
import kotlinx.coroutines.launch

class TenantViewModel(private val tenantRepository: TenantRepository) : ViewModel() {

    // Mengambil daftar tenant
    fun getTenants() {
        viewModelScope.launch {
            val tenants = tenantRepository.getAllTenants()
            // Lakukan sesuatu dengan data tenants, misalnya simpan ke LiveData atau State
        }
    }

    // Menambahkan tenant baru
    fun addTenant(tenant: Tenant) {
        viewModelScope.launch {
            tenantRepository.addTenant(tenant)
        }
    }
}
