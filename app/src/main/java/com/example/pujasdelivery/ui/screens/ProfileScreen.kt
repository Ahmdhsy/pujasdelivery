package com.example.pujasdelivery.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pujasdelivery.SignInActivity
import com.example.pujasdelivery.api.RetrofitClient
import com.example.pujasdelivery.data.RegisterResponse
import com.example.pujasdelivery.ui.theme.Raleway
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(firebaseAuth.currentUser?.email ?: "") }
    var profilePhoto by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = File(context.cacheDir, "profile_photo.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    val idToken = tokenTask.result?.token
                    if (idToken != null) {
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
                        RetrofitClient.apiService.updateProfilePhoto("Bearer $idToken", body)
                            .enqueue(object : Callback<RegisterResponse> {
                                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                                    if (response.isSuccessful && response.body()?.status == "success") {
                                        profilePhoto = response.body()?.data?.profilePhoto
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Gagal mengunggah foto: ${response.message()}"
                                        Log.e("ProfileScreen", "Upload error: ${response.code()} - ${response.message()}")
                                    }
                                }

                                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                                    errorMessage = "Error jaringan saat mengunggah foto: ${t.message}"
                                    Log.e("ProfileScreen", "Network error: ${t.message}", t)
                                }
                            })
                    } else {
                        errorMessage = "Gagal mendapatkan token"
                    }
                } else {
                    errorMessage = "Gagal mendapatkan token: ${tokenTask.exception?.message}"
                }
            }
        }
    }

    // Ambil data pengguna dari backend
    LaunchedEffect(Unit) {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val idToken = tokenTask.result?.token
                if (idToken != null) {
                    RetrofitClient.apiService.getUser("Bearer $idToken").enqueue(object : Callback<RegisterResponse> {
                        override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                            if (response.isSuccessful && response.body()?.status == "success") {
                                name = response.body()?.data?.name ?: ""
                                editedName = name
                                email = response.body()?.data?.email ?: firebaseAuth.currentUser?.email ?: ""
                                profilePhoto = response.body()?.data?.profilePhoto
                            } else {
                                errorMessage = "Gagal memuat data pengguna: ${response.message()}"
                                Log.e("ProfileScreen", "Error: ${response.code()} - ${response.message()}")
                            }
                            isLoading = false
                        }

                        override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                            errorMessage = "Error jaringan: ${t.message}"
                            Log.e("ProfileScreen", "Network error: ${t.message}", t)
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

    // Fungsi untuk menyimpan perubahan nama
    fun saveName() {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val idToken = tokenTask.result?.token
                if (idToken != null) {
                    RetrofitClient.apiService.updateUserName("Bearer $idToken", mapOf("name" to editedName))
                        .enqueue(object : Callback<RegisterResponse> {
                            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                                if (response.isSuccessful && response.body()?.status == "success") {
                                    name = editedName
                                    isEditing = false
                                    errorMessage = null
                                } else {
                                    errorMessage = "Gagal memperbarui nama: ${response.message()}"
                                    Log.e("ProfileScreen", "Update error: ${response.code()} - ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                                errorMessage = "Error jaringan saat memperbarui nama: ${t.message}"
                                Log.e("ProfileScreen", "Network error: ${t.message}", t)
                            }
                        })
                } else {
                    errorMessage = "Gagal mendapatkan token untuk pembaruan"
                }
            } else {
                errorMessage = "Gagal mendapatkan token: ${tokenTask.exception?.message}"
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditing) "Edit Profil" else "Profil Saya",
                style = TextStyle(
                    fontFamily = Raleway,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(36.dp)) // Jarak dengan foto profil

            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C3755)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhoto != null) {
                        AsyncImage(
                            model = "http://10.0.2.2:8000/storage/$profilePhoto",
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }
                }
                if (isEditing) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Photo",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.White, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) // Outline biru
                            .padding(4.dp) // Padding untuk ikon terlihat jelas
                            .clickable { imagePickerLauncher.launch("image/*") },
                        tint = MaterialTheme.colorScheme.primary // Warna ikon biru
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Jarak dengan nama

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    // Nama
                    if (isEditing) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nama",
                                style = TextStyle(
                                    fontFamily = Raleway,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp)) // Jarak dengan field
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                textStyle = TextStyle(
                                    fontFamily = Raleway,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = Color.White,
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = name,
                            style = TextStyle(
                                fontFamily = Raleway,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Jarak antara nama dan email

                    // Email
                    if (isEditing) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Email",
                                style = TextStyle(
                                    fontFamily = Raleway,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // Jarak dengan field
                            OutlinedTextField(
                                value = email,
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                textStyle = TextStyle(
                                    fontFamily = Raleway,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                readOnly = true,
                                enabled = false,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = Color.White,
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTextColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = email,
                            style = TextStyle(
                                fontFamily = Raleway,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Dorong tombol ke bawah

                    // Tombol
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp), // Jarak dengan email
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isEditing) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Batalkan",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Raleway
                                )
                            }
                            Button(
                                onClick = {
                                    if (editedName.isNotBlank()) {
                                        saveName()
                                    } else {
                                        errorMessage = "Nama tidak boleh kosong"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Simpan",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Raleway
                                )
                            }
                        } else {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Edit",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Raleway
                                )
                            }
                            Button(
                                onClick = {
                                    sharedPreferences.edit().clear().apply()
                                    firebaseAuth.signOut()
                                    val intent = Intent(context, SignInActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC3545)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Keluar Akun",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Raleway
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}