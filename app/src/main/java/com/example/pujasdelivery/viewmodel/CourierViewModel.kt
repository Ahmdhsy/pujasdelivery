package com.example.pujasdelivery.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.api.MenuApiService
import com.example.pujasdelivery.data.Gedung
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.data.TransactionData
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.TransactionStatusRequest
import com.example.pujasdelivery.MyApplication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

enum class LoadingState {
    LOADING,
    SUCCESS,
    ERROR
}

class CourierViewModel(
    private val apiService: ApiService,
    private val menuApiService: MenuApiService
) : ViewModel() {
    private val _ongoingTransactions = MutableStateFlow<List<TransactionData>>(emptyList())
    val ongoingTransactions: StateFlow<List<TransactionData>> = _ongoingTransactions.asStateFlow()

    private val _historyTransactions = MutableStateFlow<List<TransactionData>>(emptyList())
    val historyTransactions: StateFlow<List<TransactionData>> = _historyTransactions.asStateFlow()

    private val _menus = MutableStateFlow<List<Menu>>(emptyList())
    val menus: StateFlow<List<Menu>> = _menus.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _gedungs = MutableStateFlow<List<Gedung>>(emptyList())
    val gedungs: StateFlow<List<Gedung>> = _gedungs.asStateFlow()

    private val _loadingState = MutableStateFlow(LoadingState.LOADING)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    init {
        Log.d("CourierViewModel", "Initializing ViewModel")
        loadMenus()
        loadTenants()
        loadGedungs()
        loadOngoingTransactions()
        loadHistoryTransactions()
    }

    private suspend fun waitForToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return try {
            val result = user?.getIdToken(true)?.await()
            val token = result?.token
            MyApplication.token = token
            Log.d("CourierViewModel", "Fresh token obtained: $token")
            token
        } catch (e: Exception) {
            Log.e("CourierViewModel", "Failed to get token: ${e.message}")
            null
        }
    }

    fun loadOngoingTransactions() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            val token = waitForToken()
            if (token == null) {
                Log.e("CourierViewModel", "Skipping loadOngoingTransactions due to missing token")
                return@launch
            }
            try {
                Log.d("CourierViewModel", "Calling getCourierOngoingTransactions with token: $token")
                val response = apiService.getCourierOngoingTransactions(token)
                // Filter hanya transaksi dengan status "Dalam Pengantaran"
                val filteredTransactions = response.map { it.data }.filter { it.status.lowercase() == "pengantaran" }
                _ongoingTransactions.value = filteredTransactions
                _loadingState.value = LoadingState.SUCCESS
                Log.d("CourierViewModel", "Ongoing transactions loaded: ${filteredTransactions.size} items (filtered for 'Dalam Pengantaran')")
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Error loading ongoing transactions: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun loadHistoryTransactions() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            val token = waitForToken()
            if (token == null) {
                Log.e("CourierViewModel", "Skipping loadHistoryTransactions due to missing token")
                return@launch
            }
            try {
                Log.d("CourierViewModel", "Calling getCourierHistoryTransactions with token: $token")
                val response = apiService.getCourierHistoryTransactions(token)
                if (response.isNotEmpty()) {
                    _historyTransactions.value = response.map { it.data }
                    Log.d("CourierViewModel", "History transactions loaded: ${response.size} items, data=${response}")
                } else {
                    Log.w("CourierViewModel", "No history transactions returned from API")
                    _historyTransactions.value = emptyList()
                }
                _loadingState.value = LoadingState.SUCCESS
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Error loading history transactions: ${e.message}, cause=${e.cause}, stacktrace=${e.stackTraceToString()}")
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun loadMenus() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            try {
                Log.d("CourierViewModel", "Calling getMenus")
                val call = menuApiService.getMenus()
                call.enqueue(object : Callback<List<Menu>> {
                    override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                        if (response.isSuccessful) {
                            _menus.value = response.body() ?: emptyList()
                            Log.d("CourierViewModel", "Menus loaded: ${response.body()}")
                            _loadingState.value = LoadingState.SUCCESS
                        } else {
                            Log.e("CourierViewModel", "Failed to load menus: ${response.code()} - ${response.message()}")
                            _loadingState.value = LoadingState.ERROR
                        }
                    }

                    override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                        Log.e("CourierViewModel", "Network error loading menus: ${t.message}", t)
                        _loadingState.value = LoadingState.ERROR
                    }
                })
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error loading menus: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun loadTenants() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            try {
                Log.d("CourierViewModel", "Calling getTenants")
                val call = menuApiService.getTenants()
                call.enqueue(object : Callback<List<Tenant>> {
                    override fun onResponse(call: Call<List<Tenant>>, response: Response<List<Tenant>>) {
                        if (response.isSuccessful) {
                            _tenants.value = response.body() ?: emptyList()
                            Log.d("CourierViewModel", "Tenants loaded: ${response.body()}")
                            _loadingState.value = LoadingState.SUCCESS
                        } else {
                            Log.e("CourierViewModel", "Failed to load tenants: ${response.code()} - ${response.message()}")
                            _loadingState.value = LoadingState.ERROR
                        }
                    }

                    override fun onFailure(call: Call<List<Tenant>>, t: Throwable) {
                        Log.e("CourierViewModel", "Network error loading tenants: ${t.message}", t)
                        _loadingState.value = LoadingState.ERROR
                    }
                })
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error loading tenants: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun loadGedungs() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            try {
                Log.d("CourierViewModel", "Calling getBuildings")
                val call = menuApiService.getBuildings()
                call.enqueue(object : Callback<List<Gedung>> {
                    override fun onResponse(call: Call<List<Gedung>>, response: Response<List<Gedung>>) {
                        if (response.isSuccessful) {
                            _gedungs.value = response.body() ?: emptyList()
                            Log.d("CourierViewModel", "Gedungs loaded: ${response.body()}")
                            _loadingState.value = LoadingState.SUCCESS
                        } else {
                            Log.e("CourierViewModel", "Failed to load gedungs: ${response.code()} - ${response.message()}")
                            _loadingState.value = LoadingState.ERROR
                        }
                    }

                    override fun onFailure(call: Call<List<Gedung>>, t: Throwable) {
                        Log.e("CourierViewModel", "Network error loading gedungs: ${t.message}", t)
                        _loadingState.value = LoadingState.ERROR
                    }
                })
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error loading gedungs: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun updateTransactionStatus(transactionId: Int, status: String) {
        viewModelScope.launch {
            val token = waitForToken()
            if (token == null) {
                _loadingState.value = LoadingState.ERROR
                return@launch
            }
            try {
                Log.d("CourierViewModel", "Calling updateTransactionStatus with token: $token, id: $transactionId, status: $status")
                val statusRequest = TransactionStatusRequest(status)
                apiService.updateTransactionStatus(token, transactionId, statusRequest)
                loadOngoingTransactions()
                loadHistoryTransactions()
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Error updating status: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }
}