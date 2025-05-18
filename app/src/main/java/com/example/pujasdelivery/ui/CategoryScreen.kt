package com.example.pujasdelivery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pujasdelivery.data.CartItem
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    category: String,
    viewModel: DashboardViewModel,
    navController: NavController
) {
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val totalItemCount by viewModel.totalItemCount.observeAsState(initial = 0)
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0)
    var searchQuery by remember { mutableStateOf("") }

    // Load menus for the category
    LaunchedEffect(category) {
        viewModel.loadMenusByCategory(category)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { /* Handle search action if needed */ },
            active = false,
            onActiveChange = { /* Handle active state if needed */ },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari menu...") }
        ) {
            // Optional: Add search suggestions here
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Title and Cart Info
        Text(
            text = category,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Total Items: $totalItemCount | Total Price: Rp $totalPrice",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Menu Grid
        if (menus.isEmpty()) {
            Text(
                text = "Tidak ada menu tersedia",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            val filteredMenus = menus.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredMenus.size) { index ->
                    MenuCard(
                        menu = filteredMenus[index],
                        cartItems = cartItems,
                        onAddToCart = { viewModel.addToCart(filteredMenus[index]) },
                        onRemoveFromCart = { viewModel.removeFromCart(filteredMenus[index].id) },
                        onClick = {
                            navController.navigate("menuDetail/${filteredMenus[index].name}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCard(
    menu: MenuWithTenantName,
    cartItems: List<CartItem>,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onClick: () -> Unit
) {
    val cartItem = cartItems.find { cartItem -> cartItem.menuId == menu.id }
    val quantity = cartItem?.quantity ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = "http://10.0.2.2:8080/images/${menu.id}",
                contentDescription = "Menu Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = menu.name,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Rp ${menu.price}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRemoveFromCart, enabled = quantity > 0) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove from cart")
                }
                Text(
                    text = quantity.toString(),
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