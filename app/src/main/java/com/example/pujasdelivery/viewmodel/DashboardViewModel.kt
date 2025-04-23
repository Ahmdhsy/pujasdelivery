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

                // Check if data is inconsistent (e.g., "Es Teh" has wrong category)
                val allMenus = menuDao.getAllMenusWithTenantName()
                val esTeh = allMenus.find { it.name == "Es Teh" }
                val needsReinitialization = esTeh == null || esTeh.category != "Minuman"

                if (needsReinitialization) {
                    // Clear the database
                    tenantDao.deleteAll()
                    menuDao.deleteAll()
                }

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
                            description = "Mie ayam gurih",
                            category = "Makanan"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[0].toInt(),
                            name = "Es Teh",
                            price = 5000,
                            description = "Es teh manis",
                            category = "Minuman"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[1].toInt(),
                            name = "Mie Ayam",
                            price = 16000,
                            description = "Mie ayam spesial",
                            category = "Makanan"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[1].toInt(),
                            name = "Nasi Goreng",
                            price = 20000,
                            description = "Nasi goreng spesial",
                            category = "Makanan"
                        ),
                        Menu(
                            id = 0,
                            tenantId = insertedTenantIds[0].toInt(),
                            name = "Nasi Ayam",
                            price = 18000,
                            description = "Nasi dengan ayam goreng",
                            category = "Makanan"
                        )
                    )
                    menuDao.insertAll(*menuList.toTypedArray())
                }

                loadAllMenus()
                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
                println("Error initializing data: ${e.message}")
            }
        }
    }

    private fun loadAllMenus() {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading
                val allMenus = menuDao.getAllMenusWithTenantName()
                allMenus.forEach { menu ->
                    println("Menu: ${menu.name}, Category: ${menu.category}, Tenant: ${menu.tenantName}")
                }
                _menus.value = allMenus
                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
                println("Error loading menus: ${e.message}")
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