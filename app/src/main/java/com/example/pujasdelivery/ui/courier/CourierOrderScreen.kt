package com.example.pujasdelivery.ui.courier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pujasdelivery.data.TransactionData
import com.example.pujasdelivery.viewmodel.CourierViewModel
import com.example.pujasdelivery.viewmodel.LoadingState
import org.koin.androidx.compose.koinViewModel

@Composable
fun CourierOrderScreen(
    viewModel: CourierViewModel = koinViewModel(),
    navController: NavController
) {
    val ongoingTransactions by viewModel.ongoingTransactions.collectAsState()
    val historyTransactions by viewModel.historyTransactions.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Dalam Proses") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat") }
            )
        }

        when (loadingState) {
            LoadingState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            LoadingState.ERROR -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Gagal memuat pesanan. Coba lagi.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadOngoingTransactions() }) {
                        Text("Coba Lagi")
                    }
                }
            }
            LoadingState.SUCCESS -> {
                val transactions = if (selectedTab == 0) ongoingTransactions else historyTransactions
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada pesanan.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                navController = navController,
                                tenants = tenants
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.loadOngoingTransactions()
        } else {
            viewModel.loadHistoryTransactions()
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionData,
    navController: NavController,
    tenants: List<com.example.pujasdelivery.data.Tenant>
) {
    // Map tenantId ke nama tenant
    val tenant = tenants.find { it.id == transaction.tenantId }
    val tenantName = tenant?.name ?: "Warung ${transaction.tenantId.toString().padStart(2, '0')}"
    val itemsText = transaction.items.joinToString(", ") { "${it.quantity} Menu ${it.menuId}" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .clickable {
                navController.navigate("orderDetail/${transaction.id}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
                contentDescription = "Item",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tenantName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID Transaksi: ${transaction.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = itemsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Rp${transaction.totalPrice}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}