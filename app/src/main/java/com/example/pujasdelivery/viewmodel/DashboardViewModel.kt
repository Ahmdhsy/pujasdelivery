package com.example.pujasdelivery.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.data.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel : ViewModel() {
    private val apiService = RetrofitClient.menuApiService

    private val _menus = MutableLiveData<List<MenuWithTenantName>>()
    val menus: LiveData<List<MenuWithTenantName>> get() = _menus

    private val _tenants = MutableLiveData<List<Tenant>>()
    val tenants: LiveData<List<Tenant>> get() = _tenants

    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> get() = _loadingState

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _totalItemCount = MutableLiveData<Int>(0)
    val totalItemCount: LiveData<Int> get() = _totalItemCount

    private val _totalPrice = MutableLiveData<Int>(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                Log.d("DashboardViewModel", "Memulai panggilan API ke ${RetrofitClient.menuApiService.getMenus().request().url}")
                val menuResponse = withContext(Dispatchers.IO) {
                    apiService.getMenus().execute()
                }
                Log.d("DashboardViewModel", "Respons menu: ${menuResponse.code()} - ${menuResponse.message()}")
                val menusFromApi = if (menuResponse.isSuccessful) {
                    menuResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat menu: ${menuResponse.code()} - ${menuResponse.message()}")
                    emptyList()
                }

                val tenantResponse = withContext(Dispatchers.IO) {
                    apiService.getTenants().execute()
                }
                Log.d("DashboardViewModel", "Respons tenant: ${tenantResponse.code()} - ${tenantResponse.message()}")
                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                    tenantResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()} - ${tenantResponse.message()}")
                    emptyList()
                }

                val menuWithTenantNames = menusFromApi.map { menu ->
                    val tenant = tenantsFromApi.find { it.name == menu.tenant }
                    MenuWithTenantName(
                        id = menu.id,
                        tenantId = tenant?.id?.toLong() ?: 0L,
                        name = menu.name,
                        price = menu.getPriceAsInt(),
                        description = menu.deskripsi,
                        category = menu.category,
                        tenantName = menu.tenant ?: "Unknown Tenant"
                    )
                }

                _menus.postValue(menuWithTenantNames)
                _tenants.postValue(tenantsFromApi)
                _loadingState.value = LoadingState.Idle
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat data: ${e.message}", e)
                _loadingState.value = LoadingState.Error
                _menus.postValue(emptyList())
                _tenants.postValue(emptyList())
            }
        }
    }

    fun loadMenusForTenant(tenantName: String) {
        viewModelScope.launch {
            try {
                val menuResponse = withContext(Dispatchers.IO) {
                    apiService.getMenus().execute()
                }
                val menusFromApi = if (menuResponse.isSuccessful) {
                    menuResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat menu: ${menuResponse.code()}")
                    emptyList()
                }

                val tenantResponse = withContext(Dispatchers.IO) {
                    apiService.getTenants().execute()
                }
                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                    tenantResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()}")
                    emptyList()
                }

                val tenant = tenantsFromApi.find { it.name == tenantName }
                val filteredMenus = menusFromApi.filter { it.tenant == tenantName }
                val menuWithTenantNames = filteredMenus.map { menu ->
                    MenuWithTenantName(
                        id = menu.id,
                        tenantId = tenant?.id?.toLong() ?: 0L,
                        name = menu.name,
                        price = menu.getPriceAsInt(),
                        description = menu.deskripsi,
                        category = menu.category,
                        tenantName = tenantName
                    )
                }
                Log.d("DashboardViewModel", "Menus for tenant $tenantName: ${menuWithTenantNames.size}")
                _menus.postValue(menuWithTenantNames)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat menu untuk tenant: ${e.message}")
                _menus.postValue(emptyList())
            }
        }
    }

    fun loadMenusByCategory(category: String) {
        viewModelScope.launch {
            try {
                val menuResponse = withContext(Dispatchers.IO) {
                    apiService.getMenus().execute()
                }
                val menusFromApi = if (menuResponse.isSuccessful) {
                    menuResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat menu: ${menuResponse.code()}")
                    emptyList()
                }

                val tenantResponse = withContext(Dispatchers.IO) {
                    apiService.getTenants().execute()
                }
                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                    tenantResponse.body() ?: emptyList()
                } else {
                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()}")
                    emptyList()
                }

                val filteredMenus = menusFromApi.filter { it.category == category }
                val menuWithTenantNames = filteredMenus.map { menu ->
                    val tenant = tenantsFromApi.find { it.name == menu.tenant }
                    MenuWithTenantName(
                        id = menu.id,
                        tenantId = tenant?.id?.toLong() ?: 0L,
                        name = menu.name,
                        price = menu.getPriceAsInt(),
                        description = menu.deskripsi,
                        category = menu.category,
                        tenantName = menu.tenant ?: "Unknown Tenant"
                    )
                }
                Log.d("DashboardViewModel", "Menus for category $category: ${menuWithTenantNames.size}")
                _menus.postValue(menuWithTenantNames)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat menu untuk kategori: ${e.message}")
                _menus.postValue(emptyList())
            }
        }
    }

    fun addToCart(menu: MenuWithTenantName) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.menuId == menu.id }
        if (existingItem != null) {
            currentCart.remove(existingItem)
            currentCart.add(existingItem.copy(quantity = existingItem.quantity + 1))
        } else {
            currentCart.add(
                CartItem(
                    menuId = menu.id,
                    name = menu.name,
                    price = menu.price,
                    quantity = 1,
                    tenantId = menu.tenantId,
                    tenantName = menu.tenantName
                )
            )
        }
        _cartItems.value = currentCart // Use setValue for immediate update
        updateCartTotals(currentCart) // Pass currentCart to avoid race condition
    }

    fun removeFromCart(menuId: Int) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.menuId == menuId }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                currentCart.remove(existingItem)
                currentCart.add(existingItem.copy(quantity = existingItem.quantity - 1))
            } else {
                currentCart.remove(existingItem)
            }
            _cartItems.value = currentCart // Use setValue for immediate update
            updateCartTotals(currentCart) // Pass currentCart to avoid race condition
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateCartTotals(emptyList())
    }

    private fun updateCartTotals(cart: List<CartItem>) {
        val totalItems = cart.sumOf { it.quantity }
        val totalPrice = cart.sumOf { it.price * it.quantity }
        _totalItemCount.value = totalItems // Use setValue for immediate update
        _totalPrice.value = totalPrice // Use setValue for immediate update
        Log.d("DashboardViewModel", "Updated totals: items=$totalItems, price=$totalPrice")
    }

    enum class LoadingState {
        Idle, Loading, Error
    }
}