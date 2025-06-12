package com.example.pujasdelivery.ui.courier

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pujasdelivery.data.TransactionData
import com.example.pujasdelivery.viewmodel.CourierViewModel
import com.example.pujasdelivery.viewmodel.LoadingState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun CourierOrderScreen(
    viewModel: CourierViewModel = koinViewModel(),
    navController: NavController
) {
    val ongoingTransactions by viewModel.ongoingTransactions.collectAsState()
    val historyTransactions by viewModel.historyTransactions.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
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
                    LazyColumn {
                        items(transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateTransactionStatus(transaction.id, newStatus)
                                },
                                isHistory = selectedTab == 1
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
    onUpdateStatus: (String) -> Unit,
    isHistory: Boolean
) {
    var isUpdating by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (transaction.status) {
                        "diterima" -> Icons.Filled.CheckCircle
                        "diproses" -> Icons.Filled.Restaurant
                        "dalam pengantaran" -> Icons.AutoMirrored.Filled.DirectionsBike
                        "selesai" -> Icons.Filled.Done
                        "dibatalkan" -> Icons.Filled.Cancel
                        else -> Icons.Filled.Error
                    },
                    contentDescription = transaction.status,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pesanan #${transaction.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pemesan: User ID ${transaction.userId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total: Rp ${transaction.totalPrice}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Bukti Pembayaran: ${transaction.buktiPembayaran.split("/").last()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Status: ${transaction.status.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detail Pesanan:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            transaction.items.forEach { item ->
                Text(
                    text = "- Menu ID ${item.menuId} (x${item.quantity}): Rp ${item.subtotal}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (item.catatan != null) {
                    Text(
                        text = "  Catatan: ${item.catatan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (!isHistory) {
                val nextStatusOptions = when (transaction.status) {
                    "diterima" -> listOf("diproses", "dibatalkan")
                    "diproses" -> listOf("dalam pengantaran", "dibatalkan")
                    "dalam pengantaran" -> listOf("selesai", "dibatalkan")
                    else -> emptyList()
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { expanded = true },
                        enabled = nextStatusOptions.isNotEmpty() && !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Ubah Status")
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        nextStatusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    isUpdating = true
                                    onUpdateStatus(status)
                                    expanded = false
                                }
                            )
                        }
                        LaunchedEffect(isUpdating) {
                            if (isUpdating) {
                                delay(1000) // Simulasi waktu API
                                isUpdating = false
                            }
                        }
                    }
                }
            }
        }
    }
}