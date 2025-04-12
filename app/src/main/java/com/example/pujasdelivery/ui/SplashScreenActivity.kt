package com.example.pujasdelivery

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pujasdelivery.ui.OnboardingScreen
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showOnboarding by remember { mutableStateOf(false) }

            if (showOnboarding) {
                OnboardingScreen {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                SplashScreen {
                    showOnboarding = true
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3755)), // Warna brand (biru gelap)
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
