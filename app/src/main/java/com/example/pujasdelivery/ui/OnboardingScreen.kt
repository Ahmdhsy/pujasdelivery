package com.example.pujasdelivery.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pujasdelivery.R
import com.example.pujasdelivery.ui.theme.PrimaryDarkBlue

@Composable
fun OnboardingScreen(onNextClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            // Animated icons
            AnimatedCookingIcons()

            // Title
            Text(
                text = "Siap memesan makanan?",
                color = Color(0xFF2C3755),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                text = "Yuk, pesan makanan dan minuman di\nPujasera dengan lebih cepat dan mudah!",
                color = Color(0xFF2C3755),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Button
            Button(
                onClick = onNextClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Pesan sekarang!",
                    color = Color(0xFF2C3755),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun AnimatedCookingIcons() {
    // Infinite transition for animations
    val infiniteTransition = rememberInfiniteTransition(label = "cooking_animation")

    // Pan animation: slide left and right
    val panOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan_offset"
    )

    // Smoke animation: fade in and out
    val smokeOpacity by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smoke_opacity"
    )

    // Convert dp to pixels for offset
    val panOffsetDp = with(LocalDensity.current) { panOffset.toDp() }

    Box(
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Smoke icon (heat_24px) positioned above pan
        Icon(
            painter = painterResource(id = R.drawable.heat_24px),
            contentDescription = "Heat Icon",
            tint = PrimaryDarkBlue,
            modifier = Modifier
                .size(40.dp)
                .offset(y = (-30).dp)
                .graphicsLayer { alpha = smokeOpacity }
        )

        // Pan icon (skillet_cooktop_24px)
        Icon(
            painter = painterResource(id = R.drawable.skillet_cooktop_24px),
            contentDescription = "Skillet Icon",
            tint = PrimaryDarkBlue,
            modifier = Modifier
                .size(80.dp)
                .offset(x = panOffsetDp)
        )
    }
}