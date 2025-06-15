package com.example.pujasdelivery.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.MyApplication
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.Gedung
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.CartItem
import com.google.firebase.auth.FirebaseAuth
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

    // Tambahan untuk transaksi
    private val _transactions = MutableLiveData<List<TransactionResponse>>(emptyList())
    val transactions: LiveData<List<TransactionResponse>> get() = _transactions

    private val _currentTransaction = MutableLiveData<TransactionResponse?>(null)
    val currentTransaction: LiveData<TransactionResponse?> get() = _currentTransaction

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private val auth = FirebaseAuth.getInstance()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                Log.d("DashboardViewModel", "Memulai panggilan API ke ${menuApiService.getMenus().request().url}")
                menuApiService.getMenus().enqueue(object : Callback<List<Menu>> {
                    override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                        Log.d("DashboardViewModel", "Respons menu: ${response.code()} - ${response.message()}")
                        val menusFromApi = if (response.isSuccessful) {
                            response.body() ?: emptyList()
                        } else {
                            Log.e("DashboardViewModel", "Gagal memuat menu: ${response.code()} - ${response.message()} - ${response.errorBody()?.string()}")
                            emptyList()
                        }

                        // Panggilan API untuk tenants
                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
                                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()} - ${tenantResponse.message()} - ${tenantResponse.errorBody()?.string()}")
                                    emptyList()
                                }

                                // Panggilan API untuk buildings
                                menuApiService.getBuildings().enqueue(object : Callback<List<Gedung>> {
                                    override fun onResponse(call: Call<List<Gedung>>, gedungResponse: Response<List<Gedung>>) {
                                        Log.d("DashboardViewModel", "URL gedung yang dipanggil: ${call.request().url}")
                                        Log.d("DashboardViewModel", "Respons gedung: ${gedungResponse.code()} - ${gedungResponse.message()}")
                                        val gedungsFromApi = if (gedungResponse.isSuccessful) {
                                            gedungResponse.body() ?: emptyList()
                                        } else {
                                            Log.e("DashboardViewModel", "Gagal memuat gedungs: ${gedungResponse.code()} - ${gedungResponse.message()} - ${gedungResponse.errorBody()?.string() ?: "No error body"}")
                                            emptyList()
                                        }

                                        // Proses data setelah semua panggilan selesai
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
                                        _gedungs.postValue(gedungsFromApi)
                                        _loadingState.postValue(LoadingState.Idle)
                                        Log.d("DashboardViewModel", "Gedungs loaded: ${gedungsFromApi.size} items - ${gedungsFromApi.map { it.nama_gedung }}")
                                    }

                                    override fun onFailure(call: Call<List<Gedung>>, t: Throwable) {
                                        Log.e("DashboardViewModel", "Error memuat gedungs: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                                        _gedungs.postValue(emptyList())
                                        _loadingState.postValue(LoadingState.Error)
                                    }
                                })
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                Log.e("DashboardViewModel", "Error memuat tenants: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                                _tenants.postValue(emptyList())
                                _loadingState.postValue(LoadingState.Error)
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        Log.e("DashboardViewModel", "Error memuat data: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                        _loadingState.postValue(LoadingState.Error)
                        _menus.postValue(emptyList())
                        _tenants.postValue(emptyList())
                        _gedungs.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat data: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
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
                            Log.e("DashboardViewModel", "Gagal memuat menu: ${response.code()} - ${response.errorBody()?.string()}")
                            emptyList()
                        }

                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
                                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()} - ${tenantResponse.errorBody()?.string()}")
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
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                Log.e("DashboardViewModel", "Error memuat tenants: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                                _menus.postValue(emptyList())
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        Log.e("DashboardViewModel", "Error memuat menu untuk tenant: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                        _menus.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat menu untuk tenant: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
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
                            Log.e("DashboardViewModel", "Gagal memuat menu: ${response.code()} - ${response.errorBody()?.string()}")
                            emptyList()
                        }

                        menuApiService.getTenants().enqueue(object : Callback<List<Tenant>> {
                            override fun onResponse(call: Call<List<Tenant>>, tenantResponse: Response<List<Tenant>>) {
                                val tenantsFromApi = if (tenantResponse.isSuccessful) {
                                    tenantResponse.body() ?: emptyList()
                                } else {
                                    Log.e("DashboardViewModel", "Gagal memuat tenants: ${tenantResponse.code()} - ${tenantResponse.errorBody()?.string()}")
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
                            }

                            override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                                Log.e("DashboardViewModel", "Error memuat tenants: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                                _menus.postValue(emptyList())
                            }
                        })
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        Log.e("DashboardViewModel", "Error memuat menu untuk kategori: ${t.message}, Stacktrace: ${t.stackTraceToString()}")
                        _menus.postValue(emptyList())
                    }
                })
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error memuat menu untuk kategori: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
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
                                            Log.e("DashboardViewModel", "API Error: ${response.errorBody()?.string()}")
                                        }
                                        _loadingState.value = LoadingState.Idle
                                    }

                                    override fun onFailure(call: Call<List<TransactionResponse>>, t: Throwable) {
                                        _error.value = "Error jaringan: ${t.message}"
                                        Log.e("DashboardViewModel", "Network Error: ${t.stackTraceToString()}")
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
                                            Log.e("DashboardViewModel", "API Error: ${response.errorBody()?.string()}")
                                        }
                                        _loadingState.value = LoadingState.Idle
                                    }

                                    override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                                        _error.value = "Error jaringan: ${t.message}"
                                        Log.e("DashboardViewModel", "Network Error: ${t.stackTraceToString()}")
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
        Log.d("DashboardViewModel", "Updated totals: items=$totalItems, price=$totalPrice")
    }

    fun updateCartItemNote(menuId: Int, catatan: String?) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.menuId == menuId }
        if (existingItem != null) {
            currentCart.remove(existingItem)
            currentCart.add(existingItem.copy(catatan = catatan))
            _cartItems.value = currentCart
            Log.d("DashboardViewModel", "Updated note for menuId $menuId: $catatan")
        }
    }

    enum class LoadingState {
        Idle, Loading, Error
    }
}