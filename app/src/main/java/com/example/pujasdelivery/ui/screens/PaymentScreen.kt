package com.example.pujasdelivery.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.CartItem
import com.example.pujasdelivery.data.RegisterResponse
import com.example.pujasdelivery.data.TransactionResponse
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@JvmSuppressWildcards
@Composable
fun PaymentScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel,
    gedungId: Long?
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    var showUploadDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var userId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    // Log gedungId untuk debugging
    LaunchedEffect(gedungId) {
        Log.d("PaymentScreen", "gedungId: $gedungId")
    }

    // Ambil user_id dari endpoint /api/users/me
    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val idToken = tokenTask.result?.token
                if (idToken != null) {
                    RetrofitClient.apiService.getUser("Bearer $idToken").enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                            if (response.isSuccessful && response.body()?.status == "success") {
                                userId = response.body()?.data?.id
                                Log.d("PaymentScreen", "userId: ${userId}")
                            } else {
                                errorMessage = "Gagal memuat data pengguna: ${response.message()}"
                                Log.e("PaymentScreen", "Error: ${response.code()} - ${response.message()}")
                            }
                            isLoading = false
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            errorMessage = "Error jaringan: ${t.message}"
                            Log.e("PaymentScreen", "Network error: ${t.message}", t)
                            isLoading = false
                        }
                    })
                } else {
                    errorMessage = "Gagal mendapatkan token"
                    Log.e("PaymentScreen", "Token is null")
                    isLoading = false
                }
            } else {
                errorMessage = "Gagal mendapatkan token: ${tokenTask.exception?.message}"
                Log.e("PaymentScreen", "Token error: ${tokenTask.exception?.message}")
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
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
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Header with back button and title
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
                text = "Pembayaran",
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
            Text(
                text = "Pembayaran dilakukan melalui QRIS",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan QRIS untuk melanjutkan pembayaran dan pesanan akan segera diproses!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // QR Code placeholder
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QR Code",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total pembayaran: Rp. $totalPrice",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Upload proof of payment box
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clickable { showUploadDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Proof",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (imageUri == null) "Unggah bukti pembayaran" else "Bukti pembayaran terunggah",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (imageUri == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start
                    )
                }
            }

            // Note about uploading proof
            if (imageUri == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Harap unggah bukti pembayaran agar pesanan dapat diproses.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (userId == null || gedungId == null || cartItems.isEmpty() || imageUri == null) {
                        errorMessage = "Lengkapi data: pengguna, gedung, keranjang, atau bukti pembayaran."
                        Log.e("PaymentScreen", "Validation failed: userId=$userId, gedungId=$gedungId, cartItems=${cartItems.size}, imageUri=$imageUri")
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            // Ambil token secara asynchronous
                            firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val token = tokenTask.result?.token
                                    if (token == null) {
                                        errorMessage = "Gagal mendapatkan token autentikasi."
                                        isLoading = false
                                        Log.e("PaymentScreen", "Token is null")
                                        return@addOnCompleteListener
                                    }

                                    // Konversi imageUri ke File
                                    val file = imageUriToFile(imageUri!!, context)
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val buktiPembayaran = MultipartBody.Part.createFormData("bukti_pembayaran", file.name, requestFile)

                                    // Format items
                                    val itemsMap = mutableMapOf<String, RequestBody>()
                                    cartItems.forEachIndexed { index, item ->
                                        itemsMap["items[$index][menu_id]"] = item.menuId.toString().toRequestBody()
                                        itemsMap["items[$index][quantity]"] = item.quantity.toString().toRequestBody()
                                        itemsMap["items[$index][price]"] = item.price.toString().toRequestBody()
                                        itemsMap["items[$index][catatan]"] = (item.catatan ?: "").toRequestBody()
                                    }

                                    // Log request data untuk debugging
                                    Log.d("PaymentScreen", "Sending transaction: userId=$userId, tenantId=${cartItems.first().tenantId}, gedungId=$gedungId, items=$itemsMap, buktiPembayaran=${file.name}")

                                    // Kirim request
                                    RetrofitClient.apiService.createTransaction(
                                        token = "Bearer $token",
                                        userId = userId.toString().toRequestBody(),
                                        tenantId = cartItems.first().tenantId.toString().toRequestBody(),
                                        gedungId = gedungId.toString().toRequestBody(),
                                        items = itemsMap,
                                        buktiPembayaran = buktiPembayaran
                                    ).enqueue(object : Callback<TransactionResponse> {
                                        override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                                            isLoading = false
                                            if (response.isSuccessful && response.body()?.data != null) {
                                                val transactionId = response.body()!!.data.id
                                                Log.d("PaymentScreen", "Transaction created: transactionId=$transactionId")
                                                viewModel.clearCart()
                                                navController.navigate("orderConfirmation/$transactionId")
                                            } else {
                                                errorMessage = "Gagal membuat transaksi: ${response.message()}"
                                                Log.e("PaymentScreen", "API error: ${response.code()} - ${response.message()}")
                                            }
                                        }

                                        override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                                            isLoading = false
                                            errorMessage = "Error jaringan: ${t.message}"
                                            Log.e("PaymentScreen", "Network error: ${t.message}", t)
                                        }
                                    })
                                } else {
                                    isLoading = false
                                    errorMessage = "Gagal mendapatkan token: ${tokenTask.exception?.message}"
                                    Log.e("PaymentScreen", "Token error: ${tokenTask.exception?.message}")
                                }
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Terjadi kesalahan: ${e.message}"
                            Log.e("PaymentScreen", "Exception: ${e.message}", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = imageUri != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Sudah bayar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Custom dialog for uploading proof of payment
        if (showUploadDialog) {
            Dialog(
                onDismissRequest = { showUploadDialog = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = "Upload Bukti Pembayaran",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Image preview
                        Card(
                            modifier = Modifier
                                .size(150.dp)
                                .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri != null) {
                                    Text(
                                        text = "Gambar Terunggah",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = "Upload",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "Silakan unggah gambar",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Upload button
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text(
                                text = "Ambil gambar",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Buttons (Batal and Selesaikan)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showUploadDialog = false }) {
                                Text(
                                    text = "Batal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showUploadDialog = false },
                                enabled = imageUri != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Selesaikan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function untuk mengonversi Uri ke File
private fun imageUriToFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "bukti_pembayaran_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}