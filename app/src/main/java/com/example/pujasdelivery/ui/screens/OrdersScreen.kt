package com.example.pujasdelivery.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.Order
import com.example.pujasdelivery.data.CartItem
import com.example.pujasdelivery.ui.theme.PujasDeliveryTheme
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OrdersScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel
) {
    PujasDeliveryTheme {
        var selectedTab by remember { mutableStateOf(0) }
        var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            scope.launch {
                isLoading = true
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.menuApiService.getOrders().execute()
                    }
                    if (response.isSuccessful) {
                        orders = response.body() ?: emptyList()
                        errorMessage = null
                    } else {
                        errorMessage = "Gagal memuat pesanan: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                    Log.e("OrdersScreen", "Error memuat pesanan: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Pesanan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf("Dalam Proses", "Riwayat")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    val filteredOrders = when (selectedTab) {
                        0 -> orders.filter { it.status == "pending" || it.status == "cancelled" }
                        1 -> orders.filter { it.status == "completed" }
                        else -> emptyList()
                    }

                    if (filteredOrders.isEmpty()) {
                        Text(
                            text = if (selectedTab == 0) "Belum ada pesanan dalam proses" else "Belum ada riwayat pesanan",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn {
                            items(filteredOrders) { order ->
                                OrderCard(
                                    tenantName = order.tenantName,
                                    status = order.status,
                                    items = order.items,
                                    totalPrice = order.totalPrice.toString(),
                                    proofImageUri = order.proofImageUri,
                                    onClick = {
                                        navController.navigate("orderConfirmation/${order.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    tenantName: String,
    status: String,
    items: List<CartItem>,
    totalPrice: String,
    proofImageUri: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Restaurant Icon",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = tenantName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = when (status) {
                            "pending" -> "Sedang Diproses"
                            "completed" -> "Selesai"
                            "cancelled" -> "Dibatalkan"
                            else -> status
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (status) {
                            "pending" -> Color(0xFF1976D2)
                            "completed" -> Color.Green
                            "cancelled" -> Color.Red
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Column {
                        items.forEach { item ->
                            Text(
                                text = "${item.quantity} ${item.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    if (proofImageUri != null) {
                        Text(
                            text = "Bukti Pembayaran: Tersedia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "Rp $totalPrice",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}