package com.example.pujasdelivery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.Gedung
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.CartItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@JvmSuppressWildcards
class DashboardViewModel : ViewModel() {
    private val apiService: ApiService = RetrofitClient.apiService
    private val menuApiService = RetrofitClient.menuApiService

    private val _menus = MutableLiveData<List<MenuWithTenantName>>()
    val menus: LiveData<List<MenuWithTenantName>> get() = _menus

    private val _tenants = MutableLiveData<List<Tenant>>()
    val tenants: LiveData<List<Tenant>> get() = _tenants

    private val _gedungs = MutableLiveData<List<Gedung>>()
    val gedungs: LiveData<List<Gedung>> get() = _gedungs

    private val _loadingState = MutableLiveData<LoadingState>(LoadingState.Idle)
    val loadingState: LiveData<LoadingState> get() = _loadingState

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _totalItemCount = MutableLiveData<Int>(0)
    val totalItemCount: LiveData<Int> get() = _totalItemCount

    private val _totalPrice = MutableLiveData<Int>(0)
    val totalPrice: LiveData<Int> get() = _totalPrice

    private val _transactions = MutableLiveData<List<TransactionResponse>>(emptyList())
    val transactions: LiveData<List<TransactionResponse>> get() = _transactions

    private val _currentTransaction = MutableLiveData<TransactionResponse?>(null)
    val currentTransaction: LiveData<TransactionResponse?> get() = _currentTransaction

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private val _changeTenantDialogState = MutableStateFlow<ChangeTenantDialogState?>(null)
    val changeTenantDialogState: StateFlow<ChangeTenantDialogState?> get() = _changeTenantDialogState

    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                menuApiService.getMenus().enqueue(object : Callback<List<Menu>> {
                    override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                        val menusFromApi = if (response.isSuccessful) {
                            response.body() ?: emptyList()
                        } else {
                            emptyList()
                        }

                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
                                    emptyList()
                                }

                                menuApiService.getBuildings().enqueue(object : Callback<List<Gedung>> {
                                    override fun onResponse(call: Call<List<Gedung>>, gedungResponse: Response<List<Gedung>>) {
                                        val gedungsFromApi = if (gedungResponse.isSuccessful) {
                                            gedungResponse.body() ?: emptyList()
                                        } else {
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
                                                tenantName = menu.tenant ?: "Unknown Tenant",
                                                gambar = menu.gambar
                                            )
                                        }

                                        _menus.postValue(menuWithTenantNames)
                                        _tenants.postValue(tenantsFromApi)
                                        _gedungs.postValue(gedungsFromApi)
                                        _loadingState.postValue(LoadingState.Idle)
                                    }

                                    override fun onFailure(call: Call<List<Gedung>>, t: Throwable) {
                                        _gedungs.postValue(emptyList())
                                        _loadingState.postValue(LoadingState.Error)
                                    }
                                })
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                _tenants.postValue(emptyList())
                                _loadingState.postValue(LoadingState.Error)
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        _loadingState.postValue(LoadingState.Error)
                        _menus.postValue(emptyList())
                        _tenants.postValue(emptyList())
                        _gedungs.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
                _menus.postValue(emptyList())
                _tenants.postValue(emptyList())
                _gedungs.postValue(emptyList())
            }
        }
    }

    fun loadMenusForTenant(tenantName: String) {
        viewModelScope.launch {
            try {
                menuApiService.getMenus().enqueue(object : Callback<List<Menu>> {
                    override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                        val menusFromApi = if (response.isSuccessful) {
                            response.body() ?: emptyList()
                        } else {
                            emptyList()
                        }

                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
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
                                        tenantName = tenantName,
                                        gambar = menu.gambar
                                    )
                                }
                                _menus.postValue(menuWithTenantNames)
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                _menus.postValue(emptyList())
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        _menus.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                _menus.postValue(emptyList())
            }
        }
    }

    fun loadMenusByCategory(category: String) {
        viewModelScope.launch {
            try {
                menuApiService.getMenus().enqueue(object : Callback<List<Menu>> {
                    override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                        val menusFromApi = if (response.isSuccessful) {
                            response.body() ?: emptyList()
                        } else {
                            emptyList()
                        }

                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
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
                                        tenantName = menu.tenant ?: "Unknown Tenant",
                                        gambar = menu.gambar
                                    )
                                }
                                _menus.postValue(menuWithTenantNames)
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                _menus.postValue(emptyList())
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        _menus.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                _menus.postValue(emptyList())
            }
        }
    }

    fun fetchUserTransactions(status: String? = null) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                val user = auth.currentUser
                if (user != null) {
                    user.getIdToken(true).addOnSuccessListener { idTokenResult ->
                        val idToken = idTokenResult.token
                        if (idToken != null) {
                            apiService.getUserTransactions("Bearer $idToken", status ?: "")
                                .enqueue(object : Callback<List<TransactionResponse>> {
                                    override fun onResponse(
                                        call: Call<List<TransactionResponse>>,
                                        response: Response<List<TransactionResponse>>
                                    ) {
                                        if (response.isSuccessful) {
                                            _transactions.value = response.body() ?: emptyList()
                                            _error.value = null
                                        } else {
                                            _error.value = "Gagal memuat transaksi: ${response.code()} - ${response.message()}"
                                        }
                                        _loadingState.value = LoadingState.Idle
                                    }

                                    override fun onFailure(call: Call<List<TransactionResponse>>, t: Throwable) {
                                        _error.value = "Error jaringan: ${t.message}"
                                        _loadingState.value = LoadingState.Error
                                    }
                                })
                        } else {
                            _error.value = "ID Token tidak tersedia"
                            _loadingState.value = LoadingState.Error
                        }
                    }.addOnFailureListener { exception ->
                        _error.value = "Gagal mendapatkan ID Token: ${exception.message}"
                        _loadingState.value = LoadingState.Error
                    }
                } else {
                    _error.value = "Pengguna belum login"
                    _loadingState.value = LoadingState.Error
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _loadingState.value = LoadingState.Error
            }
        }
    }

    fun fetchTransaction(transactionId: Int) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                val user = auth.currentUser
                if (user != null) {
                    user.getIdToken(true).addOnSuccessListener { idTokenResult ->
                        val idToken = idTokenResult.token
                        if (idToken != null) {
                            apiService.getTransaction("Bearer $idToken", transactionId)
                                .enqueue(object : Callback<TransactionResponse> {
                                    override fun onResponse(
                                        call: Call<TransactionResponse>,
                                        response: Response<TransactionResponse>
                                    ) {
                                        if (response.isSuccessful && response.body() != null) {
                                            _currentTransaction.value = response.body()
                                            _error.value = null
                                        } else {
                                            _error.value = "Gagal memuat transaksi: ${response.message()}"
                                        }
                                        _loadingState.value = LoadingState.Idle
                                    }

                                    override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                                        _error.value = "Error jaringan: ${t.message}"
                                        _loadingState.value = LoadingState.Error
                                    }
                                })
                        } else {
                            _error.value = "ID Token tidak tersedia"
                            _loadingState.value = LoadingState.Error
                        }
                    }.addOnFailureListener { exception ->
                        _error.value = "Gagal mendapatkan ID Token: ${exception.message}"
                        _loadingState.value = LoadingState.Error
                    }
                } else {
                    _error.value = "Pengguna belum login"
                    _loadingState.value = LoadingState.Error
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _loadingState.value = LoadingState.Error
            }
        }
    }

    fun addToCart(menu: MenuWithTenantName) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        if (currentCart.isNotEmpty() && currentCart.any { it.tenantId != menu.tenantId }) {
            _changeTenantDialogState.value = ChangeTenantDialogState(
                menu = menu,
                onConfirm = {
                    // Clear cart first to ensure no items from previous tenant remain
                    _cartItems.value = emptyList()
                    updateCartTotals(emptyList())
                    // Add the new item
                    val newCart = mutableListOf<CartItem>()
                    addCartItem(menu, newCart)
                    _changeTenantDialogState.value = null
                },
                onDismiss = {
                    _changeTenantDialogState.value = null
                }
            )
        } else {
            addCartItem(menu, currentCart)
        }
    }

    private fun addCartItem(menu: MenuWithTenantName, currentCart: MutableList<CartItem>) {
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
                    tenantName = menu.tenantName,
                    gambar = menu.gambar
                )
            )
        }
        _cartItems.value = currentCart
        updateCartTotals(currentCart)
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
            _cartItems.value = currentCart
            updateCartTotals(currentCart)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateCartTotals(emptyList())
    }

    private fun updateCartTotals(cart: List<CartItem>) {
        val totalItems = cart.sumOf { it.quantity }
        val totalPrice = cart.sumOf { it.price * it.quantity }
        _totalItemCount.value = totalItems
        _totalPrice.value = totalPrice
    }

    fun updateCartItemNote(menuId: Int, catatan: String?) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.menuId == menuId }
        if (existingItem != null) {
            currentCart.remove(existingItem)
            currentCart.add(existingItem.copy(catatan = catatan))
            _cartItems.value = currentCart
        }
    }

    data class ChangeTenantDialogState(
        val menu: MenuWithTenantName,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit
    )

    enum class LoadingState {
        Idle, Loading, Error
    }
}