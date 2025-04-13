package com.example.pujasdelivery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.data.AppDatabase
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val tenantDao = AppDatabase.getDatabase(application).tenantDao()
    private val menuDao = AppDatabase.getDatabase(application).menuDao()

    val tenants: LiveData<List<Tenant>> = tenantDao.getAllTenantsLiveData()
    private val _menus = MutableLiveData<List<MenuWithTenantName>>()
    val menus: LiveData<List<MenuWithTenantName>> get() = _menus

    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> get() = _loadingState

    init {
        initializeData()
    }

    enum class LoadingState {
        Idle, Loading, Success, Error
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading

                val existingTenants = tenantDao.getAllTenants()
                if (existingTenants.isEmpty()) {
                    val tenantList = listOf(
                        Tenant(id = 0, name = "Warung XX"),
                        Tenant(id = 0, name = "Warung YY")
                    )
                    val insertedTenantIds = tenantDao.insertAll(*tenantList.toTypedArray())

                    val menuList = listOf(
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[0].toInt(),
                            name = "Mie Ayam",
                            price = 15000,
                            description = "Mie ayam gurih"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[0].toInt(),
                            name = "Es Teh",
                            price = 5000,
                            description = "Es teh manis"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[1].toInt(),
                            name = "Nasi Goreng",
                            price = 20000,
                            description = "Nasi goreng spesial"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[0].toInt(),
                            name = "Nasi Ayam",
                            price = 18000,
                            description = "Nasi dengan ayam goreng"
                        )
                    )
                    menuDao.insertAll(*menuList.toTypedArray())
                }

                val firstTenant = tenantDao.getAllTenants().firstOrNull()
                if (firstTenant != null) {
                    loadMenusForTenant(firstTenant.id)
                }

                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
            }
        }
    }

    fun loadMenusForTenant(tenantId: Int) {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading
                val tenantMenus = menuDao.getMenusWithTenantName(tenantId)
                _menus.value = tenantMenus
                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
            }
        }
    }
}