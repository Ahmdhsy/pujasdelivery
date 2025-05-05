package com.example.pujasdelivery.ui.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pujasdelivery.data.Order
import com.example.pujasdelivery.data.OrderItem as DataOrderItem
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel
) {
    // State untuk tab yang aktif
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dalam Proses", "Riwayat")

    // Ambil data pesanan dari ViewModel
    val orders by viewModel.orders.observeAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Konversi data Order ke format yang sesuai untuk UI
    val ongoingOrders = orders.filter { it.status == "Sedang Diproses" }.map { order ->
        var orderItems: List<DataOrderItem> = emptyList()
        coroutineScope.launch {
            orderItems = viewModel.getOrderDetails(order.id).second
        }
        OrderItem(
            tenantName = orderItems.joinToString(", ") { it.tenantName ?: "Unknown Tenant" },
            status = order.status,
            items = orderItems.map { "${it.quantity} ${it.menuName}" },
            totalPrice = "Rp. ${order.totalPrice}",
            orderId = order.id.toString(),
            proofImageUri = order.proofImageUri
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
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

        // TabRow untuk "Dalam Proses" dan "Riwayat"
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
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

        // Konten berdasarkan tab
        when (selectedTab) {
            0 -> {
                // Tab "Dalam Proses"
                if (ongoingOrders.isEmpty()) {
                    Text(
                        text = "Belum ada pesanan dalam proses",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(ongoingOrders) { order ->
                            OrderCard(
                                tenantName = order.tenantName,
                                status = order.status,
                                items = order.items,
                                totalPrice = order.totalPrice,
                                proofImageUri = order.proofImageUri,
                                onClick = {
                                    navController.navigate("orderConfirmation/${order.orderId}")
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Tab "Riwayat" (untuk masa depan, sementara kosong)
                Text(
                    text = "Riwayat pesanan akan ditampilkan di sini nanti",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// Data class untuk pesanan dalam proses dengan tambahan orderId dan proofImageUri
data class OrderItem(
    val tenantName: String,
    val status: String,
    val items: List<String>,
    val totalPrice: String,
    val orderId: String,
    val proofImageUri: String? = null
)

@Composable
fun OrderCard(
    tenantName: String,
    status: String,
    items: List<String>,
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
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status == "Sedang Diproses") Color(0xFF1976D2) else Color.Red
                    )
                    items.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
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
                    text = totalPrice,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}