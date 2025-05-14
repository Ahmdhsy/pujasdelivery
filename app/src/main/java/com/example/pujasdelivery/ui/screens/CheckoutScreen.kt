package com.example.pujasdelivery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.viewmodel.CartItem
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(viewModel: DashboardViewModel, navController: NavController) {
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val totalItemCount by viewModel.totalItemCount.observeAsState(initial = 0)
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0)
    var customerName by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var submissionMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cart Items
            if (cartItems.isEmpty()) {
                Text(
                    text = "Keranjang kosong",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems.size) { index ->
                        val cartItem = cartItems[index]
                        val menu = menus.find { it.id == cartItem.menuId }
                        if (menu != null) {
                            CheckoutItemCard(
                                cartItem = cartItem,
                                onAddToCart = { viewModel.addToCart(menu) },
                                onRemoveFromCart = { viewModel.removeFromCart(cartItem.menuId) }
                            )
                        }
                    }
                }
            }

            // Customer Info
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = deliveryAddress,
                onValueChange = { deliveryAddress = it },
                label = { Text("Alamat Pengiriman") },
                modifier = Modifier.fillMaxWidth()
            )

            // Total and Submit
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Total Items: $totalItemCount | Total Price: Rp $totalPrice",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Button(
                onClick = {
                    if (customerName.isNotBlank() && deliveryAddress.isNotBlank() && cartItems.isNotEmpty()) {
                        submissionMessage = "Pesanan sedang diproses..."
                        viewModel.clearCart()
                        submissionMessage = "Pesanan berhasil dikirim!"
                        navController.navigate("orderConfirmation/0")
                    } else {
                        submissionMessage = "Lengkapi nama, alamat, dan pastikan keranjang tidak kosong."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kirim Pesanan")
            }

            if (submissionMessage.isNotBlank()) {
                Text(
                    text = submissionMessage,
                    color = if (submissionMessage.contains("berhasil")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CheckoutItemCard(
    cartItem: CartItem,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = cartItem.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Rp ${cartItem.price} x ${cartItem.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRemoveFromCart, enabled = cartItem.quantity > 0) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove from cart")
                }
                Text(
                    text = cartItem.quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onAddToCart) {
                    Icon(Icons.Default.Add, contentDescription = "Add to cart")
                }
            }
        }
    }
}