package com.example.pujasdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class OrderItem(
    val warungName: String,
    val status: String,
    val items: String,
    val price: String,
    val canReorder: Boolean // Menentukan apakah tombol "Pesan Lagi" tersedia
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavHostController) {
    // State untuk tab yang dipilih
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dalam Proses", "Riwayat")

    // Data statis untuk simulasi daftar pesanan
    val ordersInProgress = listOf(
        OrderItem(
            warungName = "Warung 01, Warung 02",
            status = "Sedang Diproses",
            items = "1 Mie Ayam, 2 Nasi Goreng, ...",
            price = "RpXX.XXX",
            canReorder = false
        )
    )

    val ordersHistory = listOf(
        OrderItem(
            warungName = "Warung 01, Warung 02",
            status = "Selesai",
            items = "1 Mie Ayam, 2 Nasi Goreng, ...",
            price = "RpXX.XXX",
            canReorder = true
        ),
        OrderItem(
            warungName = "Warung 11, Warung 09, ...",
            status = "Selesai",
            items = "3 Indomie, 2 Es Teh",
            price = "RpXX.XXX",
            canReorder = true
        ),
        OrderItem(
            warungName = "Warung 01",
            status = "Dibatalkan",
            items = "1 Es Teh",
            price = "RpXX.XXX",
            canReorder = true
        ),
        OrderItem(
            warungName = "Warung 07",
            status = "Selesai",
            items = "3 Mie Ayam, 2 Es Teh",
            price = "RpXX.XXX",
            canReorder = true
        )
    )

    // Pilih daftar pesanan berdasarkan tab yang aktif
    val currentOrders = if (selectedTab == 0) ordersInProgress else ordersHistory

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PESANAN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // BottomNavigationBar sudah ditangani di NavigationSetup
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    )
                }
            }

            // Daftar Pesanan
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(currentOrders) { order ->
                    OrderListItem(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: OrderItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Warung
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C3755)) // Warna biru gelap sesuai Figma
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Warung Icon",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informasi Pesanan
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = order.warungName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = order.items,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = Color.Gray
                )
            }

            // Harga dan Tombol
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = order.price,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (order.canReorder) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Handle reorder action */ },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF4A261) // Warna oranye sesuai Figma
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Pesan Lagi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3755) // Warna teks biru gelap
                        )
                    }
                }
            }
        }
    }
}