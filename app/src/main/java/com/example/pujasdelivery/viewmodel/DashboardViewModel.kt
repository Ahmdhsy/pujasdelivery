package com.example.pujasdelivery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.AppDatabase
import com.example.pujasdelivery.data.CartItem
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val tenantDao by lazy { AppDatabase.getDatabase(application).tenantDao() }
    private val menuDao by lazy { AppDatabase.getDatabase(application).menuDao() }
    private val cartDao by lazy { AppDatabase.getDatabase(application).cartDao() }

    private val _tenants = MutableLiveData<List<Tenant>>()
    val tenants: LiveData<List<Tenant>> get() = _tenants

    private val _menus = MutableLiveData<List<MenuWithTenantName>>()
    val menus: LiveData<List<MenuWithTenantName>> get() = _menus

    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> get() = _loadingState

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _totalItemCount = MutableLiveData<Int>(0)
    val totalItemCount: LiveData<Int> get() = _totalItemCount

    private val _totalPrice = MutableLiveData<Int>(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    init {
        println("DashboardViewModel initialized, calling initializeData...")
        initializeData()
    }

    enum class LoadingState {
        Idle, Loading, Success, Error
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading
                println("Starting API calls in initializeData...")

                // Inisialisasi LiveData dari database di thread IO
                withContext(Dispatchers.IO) {
                    val cartItems = cartDao.getAllCartItems().first()
                    _cartItems.postValue(cartItems)
                    _totalItemCount.postValue(cartItems.sumOf { it.quantity })
                    _totalPrice.postValue(cartItems.sumOf { it.price * it.quantity })
                }

                // Panggil API untuk tenant
                println("Calling getTenants...")
                val tenantCall = RetrofitClient.menuApiService.getTenants()
                val tenantResponse = try {
                    withContext(Dispatchers.IO) { tenantCall.execute() }
                } catch (e: Exception) {
                    println("Tenant API call failed: ${e.message}")
                    null
                }
                val tenants = if (tenantResponse?.isSuccessful == true) {
                    val tenantList = tenantResponse.body() ?: emptyList()
                    println("Tenants fetched: $tenantList")
                    withContext(Dispatchers.IO) {
                        tenantDao.deleteAll()
                        tenantDao.insertAll(*tenantList.map { it.copy(id = it.id) }.toTypedArray())
                        tenantList
                    }
                } else {
                    println("Failed to fetch tenants: ${tenantResponse?.code()} - ${tenantResponse?.message()}")
                    withContext(Dispatchers.IO) {
                        initializeStaticTenants()
                        tenantDao.getAllTenants()
                    }
                }
                _tenants.value = tenants

                // Panggil API untuk menu
                println("Calling getMenus...")
                val menuCall = RetrofitClient.menuApiService.getMenus()
                val menuResponse = try {
                    val response = withContext(Dispatchers.IO) { menuCall.execute() }
                    println("Raw menu response: ${response.raw()}")
                    response
                } catch (e: Exception) {
                    println("Menu API call failed: ${e.message}")
                    null
                }
                val menus = if (menuResponse?.isSuccessful == true) {
                    val apiResponse = menuResponse.body()
                    if (apiResponse?.status == true) {
                        val menuList = apiResponse.data
                        println("Menus fetched: $menuList")
                        withContext(Dispatchers.IO) {
                            menuDao.deleteAll()
                            menuDao.insertAll(*menuList.map { it.copy(id = it.id) }.toTypedArray())
                            menuList
                        }
                    } else {
                        println("API response status false: ${apiResponse?.message}")
                        emptyList()
                    }
                } else {
                    println("Failed to fetch menus: ${menuResponse?.code()} - ${menuResponse?.message()}")
                    withContext(Dispatchers.IO) {
                        initializeStaticMenus()
                        emptyList()
                    }
                }

                // Gabungkan data untuk MenuWithTenantName
                val tenantMap = tenants.associateBy { it.id }
                val menuWithTenantNames = menus.map { menu ->
                    MenuWithTenantName(
                        id = menu.id,
                        tenantId = menu.tenantId,
                        name = menu.name,
                        price = menu.getPriceAsInt(),
                        description = menu.description,
                        category = menu.category,
                        tenantName = tenantMap[menu.tenantId]?.name ?: "Unknown Tenant"
                    )
                }
                println("Menus with tenant names: $menuWithTenantNames")
                _menus.value = menuWithTenantNames

                loadAllMenus()
                _loadingState.value = LoadingState.Success
                println("API calls completed successfully")
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
                println("Error initializing data: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.IO) {
                    initializeStaticTenants()
                    initializeStaticMenus()
                    loadAllMenus()
                }
            }
        }
    }

    private suspend fun initializeStaticTenants() {
        val existingTenants = withContext(Dispatchers.IO) { tenantDao.getAllTenants() }
        if (existingTenants.isEmpty()) {
            val tenantList = listOf(
                Tenant(
                    id = 0,
                    name = "Warung XX",
                    description = "Warung makan favorit",
                    imageURL = "https://example.com/warung_xx.jpg"
                ),
                Tenant(
                    id = 0,
                    name = "Warung YY",
                    description = "Warung spesial",
                    imageURL = "https://example.com/warung_yy.jpg"
                )
            )
            withContext(Dispatchers.IO) {
                tenantDao.insertAll(*tenantList.toTypedArray())
            }
            println("Static tenants initialized: $tenantList")
        }
    }

    private suspend fun initializeStaticMenus() {
        val existingMenus = withContext(Dispatchers.IO) { menuDao.getAllMenusWithTenantName() }
        if (existingMenus.isEmpty()) {
            println("No static menus initialized; relying on API data")
        }
    }

    private fun loadAllMenus() {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading
                val allMenus = withContext(Dispatchers.IO) {
                    menuDao.getAllMenusWithTenantName()
                }
                allMenus.forEach { menu ->
                    println("Menu: ${menu.name}, Category: ${menu.category}, Tenant: ${menu.tenantName}")
                }
                _menus.value = allMenus
                _loadingState.value = LoadingState.Success
                println("Loaded all menus from database: $allMenus")
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
                val tenantMenus = withContext(Dispatchers.IO) {
                    menuDao.getMenusWithTenantName(tenantId)
                }
                _menus.value = tenantMenus
                _loadingState.value = LoadingState.Success
                println("Loaded menus for tenant $tenantId: $tenantMenus")
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
                println("Error loading tenant menus: ${e.message}")
            }
        }
    }

    fun addToCart(menu: MenuWithTenantName) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val currentItems = cartDao.getAllCartItems().first()
                    val existingItem = currentItems.find {
                        it.menuId == menu.id && it.tenantName == menu.tenantName
                    }

                    if (existingItem != null) {
                        val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                        cartDao.update(updatedItem)
                        println("Updated cart item: $updatedItem")
                    } else {
                        val cartItem = CartItem(
                            menuId = menu.id,
                            menuName = menu.name,
                            tenantId = menu.tenantId,
                            tenantName = menu.tenantName,
                            price = menu.price,
                            quantity = 1
                        )
                        cartDao.insert(cartItem)
                        println("Added to cart: $cartItem")
                    }
                    val updatedCartItems = cartDao.getAllCartItems().first()
                    _cartItems.postValue(updatedCartItems)
                    _totalItemCount.postValue(updatedCartItems.sumOf { it.quantity })
                    _totalPrice.postValue(updatedCartItems.sumOf { it.price * it.quantity })
                }
            } catch (e: Exception) {
                println("Error adding to cart: ${e.message}")
            }
        }
    }

    fun removeFromCart(menu: MenuWithTenantName) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val currentItems = cartDao.getAllCartItems().first()
                    val existingItem = currentItems.find {
                        it.menuId == menu.id && it.tenantName == menu.tenantName
                    }

                    if (existingItem != null) {
                        if (existingItem.quantity > 1) {
                            val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1)
                            cartDao.update(updatedItem)
                            println("Updated cart item: $updatedItem")
                        } else {
                            cartDao.delete(existingItem)
                            println("Removed from cart: $existingItem")
                        }
                    }
                    val updatedCartItems = cartDao.getAllCartItems().first()
                    _cartItems.postValue(updatedCartItems)
                    _totalItemCount.postValue(updatedCartItems.sumOf { it.quantity })
                    _totalPrice.postValue(updatedCartItems.sumOf { it.price * it.quantity })
                }
            } catch (e: Exception) {
                println("Error removing from cart: ${e.message}")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    cartDao.clearCart()
                    println("Cart cleared")
                    val updatedCartItems = cartDao.getAllCartItems().first()
                    _cartItems.postValue(updatedCartItems)
                    _totalItemCount.postValue(0)
                    _totalPrice.postValue(0)
                }
            } catch (e: Exception) {
                println("Error clearing cart: ${e.message}")
            }
        }
    }
}