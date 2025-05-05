package com.example.pujasdelivery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@Composable
fun CategoryScreen(
    category: String,
    viewModel: DashboardViewModel,
    navController: NavHostController
) {
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val loadingState by viewModel.loadingState.observeAsState(initial = DashboardViewModel.LoadingState.Idle)
    val totalItemCount by viewModel.totalItemCount.observeAsState(initial = 0)
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with back button and category title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                    text = category,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(40.dp)) // Placeholder for symmetry
            }

            // Search bar (reusing the one from DashboardScreen)
            SearchBar(
                searchQuery = "",
                onSearchQueryChange = {},
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Handle loading state
            when (loadingState) {
                DashboardViewModel.LoadingState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                DashboardViewModel.LoadingState.Error -> {
                    Text(
                        text = "Terjadi kesalahan saat memuat data",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    // Log all menus before filtering
                    println("All Menus: ${menus.map { "${it.name} (${it.category})" }}")

                    // Filter menus based on the category (case-insensitive)
                    val filteredMenus = menus.filter { menu ->
                        menu.category.equals(category, ignoreCase = true)
                    }

                    // Log the filtered menus
                    println("Category: $category, Filtered Menus: ${filteredMenus.map { it.name }}")

                    // Check if filteredMenus is empty and display a message if it is
                    if (filteredMenus.isEmpty()) {
                        Text(
                            text = "Tidak ada menu di kategori $category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    } else {
                        // List of menu items
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 72.dp), // Add padding to avoid overlap with floating footer
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredMenus) { menu ->
                                CategoryMenuCard(
                                    menu = menu,
                                    viewModel = viewModel,
                                    onAddClick = { viewModel.addToCart(menu) },
                                    onRemoveClick = { viewModel.removeFromCart(menu) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating footer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.BottomCenter)
                .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // White background
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalItemCount Item",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Rp. $totalPrice",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { navController.navigate("checkout") },
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryMenuCard(
    menu: MenuWithTenantName,
    viewModel: DashboardViewModel,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    // Observe the cart items to get the quantity of this specific menu item
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val itemCount = cartItems.find { it.menuId == menu.id && it.tenantName == menu.tenantName }?.quantity ?: 0

    // Log to confirm the card is being rendered
    println("Rendering CategoryMenuCard for: ${menu.name}, Quantity in cart: $itemCount")

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
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image placeholder with rounded corners
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                Text(
                    text = "Image",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menu.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = menu.tenantName ?: "Unknown Tenant",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Rp. ${menu.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (itemCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onRemoveClick()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = "-",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = "$itemCount",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = {
                                onAddClick()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = "+",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onAddClick()
                        },
                        modifier = Modifier
                            .height(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Tambah",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}