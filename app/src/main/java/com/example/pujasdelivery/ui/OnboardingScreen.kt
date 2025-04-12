package com.example.pujasdelivery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onNextClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = 80.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Siap memesan makanan?",
                color = Color(0xFF2C3755),
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Yuk, pesan makanan dan minuman di\nPujasera dengan lebih cepat dan mudah!",
                color = Color(0xFF2C3755),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNextClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261)),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Pesan sekarang!", color = Color(0xFF2C3755))
            }
        }
    }
}
