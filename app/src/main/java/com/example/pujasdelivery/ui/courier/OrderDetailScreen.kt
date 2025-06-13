package com.example.pujasdelivery.ui.courier

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pujasdelivery.data.Menu
import com.example.pujasdelivery.data.TransactionData
import com.example.pujasdelivery.data.TransactionItem
import com.example.pujasdelivery.viewmodel.CourierViewModel
import com.example.pujasdelivery.viewmodel.LoadingState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun OrderDetailScreen(
    navController: NavHostController,
    viewModel: CourierViewModel = koinViewModel(),
    orderId: String
) {
    val ongoingTransactions by viewModel.ongoingTransactions.collectAsState()
    val historyTransactions by viewModel.historyTransactions.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val menus by viewModel.menus.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var transaction by remember { mutableStateOf<TransactionData?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) } // Untuk konfirmasi pembatalan
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        coroutineScope.launch {
            Log.d("OrderDetailScreen", "Menus size: ${menus.size}, Menus: $menus")
            if (menus.isEmpty()) {
                viewModel.loadMenus()
            }
            val allTransactions = ongoingTransactions + historyTransactions
            transaction = allTransactions.find { it.id.toString() == orderId }
            if (transaction == null) {
                viewModel.loadOngoingTransactions()
                viewModel.loadHistoryTransactions()
                transaction = (ongoingTransactions + historyTransactions).find { it.id.toString() == orderId }
                if (transaction == null) {
                    Log.e("OrderDetailScreen", "Transaction with orderId $orderId not found")
                }
            }
        }
    }

    val currentTransaction = transaction

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Detail Pesanan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    if (currentTransaction != null) {
                        Text(
                            text = "ID Transaksi: ${currentTransaction.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.size(40.dp))
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
                    if (currentTransaction == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pesanan tidak ditemukan.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Alamat Gedung Pengantaran",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gedung ${currentTransaction.gedungId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nama Penerima: User ${currentTransaction.userId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Pesanan (${currentTransaction.items.sumOf { it.quantity }} menu)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Rp${String.format("%.0f", currentTransaction.totalPrice)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentTransaction.items) { item ->
                                    val menu = menus.find { it.id == item.menuId }
                                    ItemCard(
                                        item = item,
                                        menuName = menu?.name ?: "Menu ${item.menuId}"
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Bukti Pembayaran",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (currentTransaction.buktiPembayaran.isNotEmpty()) {
                                                Image(
                                                    painter = rememberAsyncImagePainter("http://10.0.2.2:8000/${currentTransaction.buktiPembayaran}"),
                                                    contentDescription = "Payment Proof",
                                                    modifier = Modifier
                                                        .size(150.dp)
                                                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                                                )
                                            } else {
                                                Text(
                                                    text = "Belum ada bukti pembayaran",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (currentTransaction.status != "Selesai" && currentTransaction.status != "Dibatalkan") {
                                Spacer(modifier = Modifier.height(16.dp))
                                val nextStatus = when (currentTransaction.status) {
                                    "diterima" -> "Diproses"
                                    "diproses" -> "Dalam Pengantaran"
                                    "dalam pengantaran" -> "Selesai"
                                    else -> null
                                }

                                if (nextStatus != null) {
                                    Button(
                                        onClick = {
                                            isUpdating = true
                                            coroutineScope.launch {
                                                viewModel.updateTransactionStatus(currentTransaction.id, nextStatus.lowercase())
                                                delay(1000) // Simulasi waktu API
                                                isUpdating = false
                                            }
                                        },
                                        enabled = !isUpdating,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))
                                    ) {
                                        if (isUpdating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text(nextStatus)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showCancelConfirmation = true },
                                    enabled = !isUpdating,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Batalkan Pesanan", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showConfirmationDialog) {
            if (showSuccessDialog) {
                Dialog(
                    onDismissRequest = { showSuccessDialog = false },
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pesanan berhasi diselesaikan!",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    navController.popBackStack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C4B4))
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            if (showCancelConfirmation) {
                Dialog(
                    onDismissRequest = { showCancelConfirmation = false },
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Anda yakin ingin membatalkan pesanan?",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { showCancelConfirmation = false },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) {
                                    Text("Tidak")
                                }
                                Button(
                                    onClick = {
                                        showCancelConfirmation = false
                                        isUpdating = true
                                        coroutineScope.launch {
                                            currentTransaction?.let {
                                                viewModel.updateTransactionStatus(it.id, "dibatalkan")
                                                delay(1000) // Simulasi waktu API
                                                isUpdating = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Ya", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: com.example.pujasdelivery.data.TransactionItem,
    menuName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
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
            verticalAlignment = Alignment.Top
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
                    text = menuName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qty: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (item.catatan != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Catatan: ${item.catatan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Rp${String.format("%.0f", item.subtotal)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}