package com.example.pujasdelivery.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pujasdelivery.api.ApiService
import com.example.pujasdelivery.data.TransactionData
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.TransactionStatusRequest
import com.example.pujasdelivery.MyApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class LoadingState {
    LOADING,
    SUCCESS,
    ERROR
}

class CourierViewModel(private val apiService: ApiService) : ViewModel() {
    private val _ongoingTransactions = MutableStateFlow<List<TransactionData>>(emptyList())
    val ongoingTransactions: StateFlow<List<TransactionData>> = _ongoingTransactions.asStateFlow()

    private val _historyTransactions = MutableStateFlow<List<TransactionData>>(emptyList())
    val historyTransactions: StateFlow<List<TransactionData>> = _historyTransactions.asStateFlow()

    private val _loadingState = MutableStateFlow(LoadingState.LOADING)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private suspend fun waitForToken(): String? {
        var token = MyApplication.token
        var attempts = 0
        while (token == null && attempts < 5) {
            Log.d("CourierViewModel", "Waiting for token, attempt $attempts")
            delay(500)
            token = MyApplication.token
            attempts++
        }
        if (token == null) {
            Log.e("CourierViewModel", "Token not available after $attempts attempts")
        }
        return token
    }

    fun loadOngoingTransactions() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            val token = waitForToken()
            if (token == null) {
                _loadingState.value = LoadingState.ERROR
                return@launch
            }
            try {
                Log.d("CourierViewModel", "Calling getCourierOngoingTransactions with token: $token")
                val response = apiService.getCourierOngoingTransactions(token)
                _ongoingTransactions.value = response.map { it.data }
                _loadingState.value = LoadingState.SUCCESS
            } catch (e: HttpException) {
                Log.e("CourierViewModel", "HTTP Error loading ongoing transactions: ${e.code()} - ${e.message()}", e)
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourierViewModel", "Response body: $errorBody")
                if (errorBody?.contains("<html") == true) {
                    Log.e("CourierViewModel", "Server returned HTML error page, check backend logs")
                }
                _loadingState.value = LoadingState.ERROR
            } catch (e: IOException) {
                Log.e("CourierViewModel", "Network error loading ongoing transactions: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error loading ongoing transactions: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun loadHistoryTransactions() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            val token = waitForToken()
            if (token == null) {
                _loadingState.value = LoadingState.ERROR
                return@launch
            }
            try {
                Log.d("CourierViewModel", "Calling getCourierHistoryTransactions with token: $token")
                val response = apiService.getCourierHistoryTransactions(token)
                _historyTransactions.value = response.map { it.data }
                _loadingState.value = LoadingState.SUCCESS
            } catch (e: HttpException) {
                Log.e("CourierViewModel", "HTTP Error loading history transactions: ${e.code()} - ${e.message()}", e)
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourierViewModel", "Response body: $errorBody")
                if (errorBody?.contains("<html") == true) {
                    Log.e("CourierViewModel", "Server returned HTML error page, check backend logs")
                }
                _loadingState.value = LoadingState.ERROR
            } catch (e: IOException) {
                Log.e("CourierViewModel", "Network error loading history transactions: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error loading history: ${e.message}", e)
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
            } catch (e: HttpException) {
                Log.e("CourierViewModel", "HTTP Error updating status: ${e.code()} - ${e.message()}", e)
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourierViewModel", "Response body: $errorBody")
                if (errorBody?.contains("<html") == true) {
                    Log.e("CourierViewModel", "Server returned HTML error page, check backend logs")
                }
                _loadingState.value = LoadingState.ERROR
            } catch (e: IOException) {
                Log.e("CourierViewModel", "Network error updating status: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            } catch (e: Exception) {
                Log.e("CourierViewModel", "Unexpected error updating status: ${e.message}", e)
                _loadingState.value = LoadingState.ERROR
            }
        }
    }
}