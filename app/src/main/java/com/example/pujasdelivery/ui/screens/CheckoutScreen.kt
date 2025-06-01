package com.example.pujasdelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel
) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                        )
                    }
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "Detail Pengiriman",
                    style = MaterialTheme.typography.bodyLarge.copy(
                    ),
                )
                        modifier = Modifier
                    )
                    Text(
                    )
                            Text(
                                style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                    )
                        Text(
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                        )
                )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge.copy(
                        )
                        Text(
                            text = "Rp. $totalPrice",
                            style = MaterialTheme.typography.bodyLarge.copy(
                        )
                    }
                    Row(
                    ) {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.bodyLarge.copy(
                        )
                        Text(
                            text = "Rp. $totalPrice",
                            style = MaterialTheme.typography.bodyLarge.copy(
                        )
                    }
                    Button(
                        onClick = {
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        Text(
                            color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }