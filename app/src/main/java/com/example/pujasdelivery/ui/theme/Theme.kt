package com.example.pujasdelivery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = PrimaryDarkBlue,
    onPrimary = BackgroundWhite,
    background = BackgroundWhite,
    onBackground = PrimaryDarkBlue,
    secondary = SecondaryGray,
    onSecondary = BackgroundWhite,
    tertiary = AccentOrange,
    error = StatusWarningRed,
    onError = BackgroundWhite,
    surface = BackgroundWhite,
    onSurface = PrimaryDarkBlue
)

@Composable
fun PujasDeliveryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography, // Pastikan menggunakan Typography yang sudah didefinisikan
        content = content
    )
}