package com.example.pujasdelivery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.data.AppDatabase
import com.example.pujasdelivery.data.CartItem
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val tenantDao = AppDatabase.getDatabase(application).tenantDao()
    private val menuDao = AppDatabase.getDatabase(application).menuDao()
    private val cartDao = AppDatabase.getDatabase(application).cartDao()

    val tenants: LiveData<List<Tenant>> = tenantDao.getAllTenantsLiveData()
    private val _menus = MutableLiveData<List<MenuWithTenantName>>()
    val menus: LiveData<List<MenuWithTenantName>> get() = _menus

    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> get() = _loadingState

    // Cart-related LiveData
    val cartItems: LiveData<List<CartItem>> = cartDao.getAllCartItems().asLiveData(viewModelScope.coroutineContext)
    val totalItemCount: LiveData<Int> = cartItems.map { items -> items.sumOf { it.quantity } }
    val totalPrice: LiveData<Int> = cartItems.map { items -> items.sumOf { it.price * it.quantity } }

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

    // Cart management functions
    fun addToCart(menu: MenuWithTenantName) {
        viewModelScope.launch {
            try {
                // Fetch the current cart items
                val currentItems = cartDao.getAllCartItems().first()
                val existingItem = currentItems.find {
                    it.menuId == menu.id && it.tenantName == menu.tenantName
                }

                if (existingItem != null) {
                    // Update quantity
                    val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                    cartDao.update(updatedItem)
                    println("Updated cart item: $updatedItem")
                } else {
                    // Add new item
                    val cartItem = CartItem(
                        menuId = menu.id,
                        menuName = menu.name,
                        tenantId = menu.tenantId, // Include tenantId
                        tenantName = menu.tenantName,
                        price = menu.price,
                        quantity = 1
                    )
                    cartDao.insert(cartItem)
                    println("Added to cart: $cartItem")
                }
            } catch (e: Exception) {
                println("Error adding to cart: ${e.message}")
            }
        }
    }

    fun removeFromCart(menu: MenuWithTenantName) {
        viewModelScope.launch {
            try {
                // Fetch the current cart items
                val currentItems = cartDao.getAllCartItems().first()
                val existingItem = currentItems.find {
                    it.menuId == menu.id && it.tenantName == menu.tenantName
                }

                if (existingItem != null) {
                    if (existingItem.quantity > 1) {
                        // Decrease quantity
                        val updatedItem = existingItem.copy(quantity = existingItem.quantity - 1)
                        cartDao.update(updatedItem)
                        println("Updated cart item: $updatedItem")
                    } else {
                        // Remove item if quantity is 1
                        cartDao.delete(existingItem)
                        println("Removed from cart: $existingItem")
                    }
                }
            } catch (e: Exception) {
                println("Error removing from cart: ${e.message}")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartDao.clearCart()
                println("Cart cleared")
            } catch (e: Exception) {
                println("Error clearing cart: ${e.message}")
            }
        }
    }
}