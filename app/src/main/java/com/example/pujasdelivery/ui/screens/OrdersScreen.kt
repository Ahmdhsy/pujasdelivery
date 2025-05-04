package com.example.pujasdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun OrdersScreen(
    navController: NavHostController
) {
    // State untuk tab yang aktif
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dalam Proses", "Riwayat")

    // Data dummy untuk pesanan dalam proses
    val ongoingOrders = listOf(
        OrderItem("Warung 01, Warung 02", "Sedang Diproses", listOf("1 Mie Ayam", "2 Nasi Goreng"), "Rp50.000"),
        OrderItem("Warung 11, Warung 09", "Sedang Diproses", listOf("3 Indomie", "2 Es Teh"), "Rp40.000"),
        OrderItem("Warung 01", "Dibatalkan", listOf("1 Es Teh"), "Rp5.000"),
        OrderItem("Warung 07", "Sedang Diproses", listOf("3 Mie Ayam", "2 Es Teh"), "Rp60.000")
    )

    // Data dummy untuk riwayat pesanan
    val orderHistory = listOf(
        OrderHistoryItem("Warung 01, Warung 02", "Selesai", listOf("1 Mie Ayam", "2 Nasi Goreng"), "Rp50.000"),
        OrderHistoryItem("Warung 11, Warung 09", "Selesai", listOf("3 Indomie", "2 Es Teh"), "Rp40.000"),
        OrderHistoryItem("Warung 01", "Dibatalkan", listOf("1 Es Teh"), "Rp5.000"),
        OrderHistoryItem("Warung 07", "Selesai", listOf("3 Mie Ayam", "2 Es Teh"), "Rp60.000")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "PESANAN",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                                onClick = {
                                    navController.navigate("orderConfirmation/1")
                                }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Tab "Riwayat"
                if (orderHistory.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat pesanan",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(orderHistory) { order ->
                            OrderHistoryCard(
                                tenantName = order.tenantName,
                                status = order.status,
                                items = order.items,
                                totalPrice = order.totalPrice,
                                onClick = {
                                    navController.navigate("checkout")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class untuk pesanan dalam proses
data class OrderItem(
    val tenantName: String,
    val status: String,
    val items: List<String>,
    val totalPrice: String
)

// Data class untuk riwayat pesanan
data class OrderHistoryItem(
    val tenantName: String,
    val status: String,
    val items: List<String>,
    val totalPrice: String
)

@Composable
fun OrderCard(
    tenantName: String,
    status: String,
    items: List<String>,
    totalPrice: String,
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
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
            }
            Text(
                text = totalPrice,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun OrderHistoryCard(
    tenantName: String,
    status: String,
    items: List<String>,
    totalPrice: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
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
                        color = if (status == "Selesai") Color(0xFF1976D2) else Color.Red
                    )
                    items.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    text = totalPrice,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .height(36.dp)
                        .width(120.dp), // Meningkatkan lebar menjadi 120.dp untuk menampung "Pesan lagi"
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Pesan lagi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}