package com.example.pujasdelivery.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavHostController
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.data.TransactionItem
import com.example.pujasdelivery.utils.StatusMapper
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun OrderConfirmationScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel,
    orderId: String,
    fromOrders: Boolean = false
) {
    val firebaseAuth = FirebaseAuth.getInstance()
    var transaction by remember { mutableStateOf<TransactionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val token = tokenTask.result?.token
                if (token != null) {
                    RetrofitClient.apiService.getTransaction("Bearer $token", orderId.toInt()).enqueue(object : Callback<TransactionResponse> {
                        override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                transaction = response.body()
                            } else {
                                errorMessage = "Gagal memuat transaksi: ${response.message()}"
                            }
                            isLoading = false
                        }

                        override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                            errorMessage = "Error jaringan: ${t.message}"
                            isLoading = false
                        }
                    })
                } else {
                    errorMessage = "Gagal mendapatkan token"
                    isLoading = false
                }
            } else {
                errorMessage = "Gagal mendapatkan token: ${tokenTask.exception?.message}"
                isLoading = false
            }
        }
    }

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
                onClick = {
                    if (fromOrders) {
                        navController.popBackStack("orders", inclusive = false)
                    } else {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Status Pesanan #${transaction?.data?.id ?: orderId}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                return@Column
            }

            if (transaction == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Transaksi tidak ditemukan",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                return@Column
            }

            Text(
                text = "Cek Status Pesanan",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pantau pesanan Anda secara real-time!",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            transaction?.data?.let { data ->
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
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Status: ${StatusMapper.mapStatusToDisplay(data.status)}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (data.status) {
                                "pending" -> "Pesanan Anda sedang menunggu konfirmasi!"
                                "diproses" -> "Pesanan Anda sedang diproses!"
                                "pengantaran" -> "Pesanan Anda sedang dalam pengantaran!"
                                "selesai" -> "Pesanan Anda telah selesai!"
                                else -> "Status tidak diketahui."
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nama Penerima: ${data.userName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Alamat Gedung: ${data.gedungName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Courier",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Budi Kurir",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val url = "https://wa.me/6281234567890"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            navController.context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text(
                            text = "Hubungi WhatsApp",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            transaction?.data?.let { data ->
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
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Pesanan (${data.items.sumOf { it.quantity }} menu)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Rp. ${data.totalPrice.toInt()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detail Pesanan",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        data.items.forEach { item ->
                            OrderMenuCard(item = item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderMenuCard(item: TransactionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = item.menuName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.tenantName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rp. ${item.price.toInt()} x ${item.quantity}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Subtotal: Rp. ${item.subtotal.toInt()}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!item.catatan.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${item.catatan}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}