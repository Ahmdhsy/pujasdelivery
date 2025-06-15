package com.example.pujasdelivery

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pujasdelivery.ui.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        firebaseAuth = FirebaseAuth.getInstance()

        setContent {
            var showOnboarding by remember { mutableStateOf(false) }
            val isOnboardingShown = sharedPreferences.getBoolean("onboarding_shown", false)

            if (showOnboarding && !isOnboardingShown) {
                OnboardingScreen {
                    // Tandai onboarding sebagai sudah ditampilkan
                    sharedPreferences.edit().putBoolean("onboarding_shown", true).apply()
                    navigateToSignIn()
                }
            } else {
                SplashScreen {
                    // Periksa status login
                    if (firebaseAuth.currentUser != null) {
                        // Pengguna sudah login, ambil token dan navigasi ke MainActivity
                        firebaseAuth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                            MyApplication.token = result.token
                            navigateToMainActivity()
                        }?.addOnFailureListener {
                            // Gagal mendapatkan token, tetap navigasi ke MainActivity
                            navigateToMainActivity()
                        }
                    } else {
                        // Pengguna belum login, tampilkan onboarding jika belum ditampilkan
                        if (!isOnboardingShown) {
                            showOnboarding = true
                        } else {
                            navigateToSignIn()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Ambil role dari SharedPreferences jika sudah tersimpan
        val role = sharedPreferences.getString("user_role", null)
        intent.putExtra("role", role)
        startActivity(intent)
        finish()
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3755)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo_2),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        LaunchedEffect(Unit) {
            delay(2000)
            onTimeout()
        }
    }
}