package com.example.pujasdelivery.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantViewModel : ViewModel() {
    private val apiService = RetrofitClient.menuApiService
    private val _tenants = MutableLiveData<List<Tenant>>()
    val tenants: LiveData<List<Tenant>> get() = _tenants

    init {
        loadTenants()
    }

    private fun loadTenants() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getTenants().execute()
                }
                if (response.isSuccessful) {
                    val tenants = response.body()?.data ?: emptyList()
                    _tenants.postValue(tenants)
                } else {
                    Log.e("TenantViewModel", "Gagal memuat tenants: ${response.code()}")
                    _tenants.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("TenantViewModel", "Error memuat tenants: ${e.message}")
                _tenants.postValue(emptyList())
            }
        }
    }

    fun addTenant(tenant: Tenant) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.addTenant(tenant).execute()
                }
                if (response.isSuccessful) {
                    Log.d("TenantViewModel", "Tenant ditambahkan: $tenant")
                    loadTenants() // Segarkan daftar tenants
                } else {
                    Log.e("TenantViewModel", "Gagal menambah tenant: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TenantViewModel", "Error menambah tenant: ${e.message}")
            }
        }
    }
}