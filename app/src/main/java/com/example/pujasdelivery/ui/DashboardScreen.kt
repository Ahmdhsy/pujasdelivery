package com.example.pujasdelivery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, navController: NavHostController) {
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val loadingState by viewModel.loadingState.observeAsState(initial = DashboardViewModel.LoadingState.Idle)
    var searchQuery by remember { mutableStateOf("") }

    // Filter menu berdasarkan query pencarian
    val filteredMenus = menus.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Text(
            text = "Temukan makanan dan minuman yang ingin Anda pesan di Pujasera POLBAN!",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Kolom Pencarian
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Kategori (tanpa judul "Kategori")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryCard(category = "Makanan") {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Makanan Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            CategoryCard(category = "Minuman") {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = "Minuman Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Daftar Menu (tanpa judul "Menu")
        when (loadingState) {
            DashboardViewModel.LoadingState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            DashboardViewModel.LoadingState.Error -> {
                Text(
                    text = "Terjadi kesalahan saat memuat data",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                if (filteredMenus.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredMenus) { menu ->
                            MenuCard(
                                menu = menu,
                                onClick = {
                                    // Navigate to a detail screen for this menu
                                    navController.navigate("menuDetail/${menu.name}")
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tidak ada menu yang ditemukan",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                text = "Cari makanan, minuman",
                color = MaterialTheme.colorScheme.secondary, // Light gray placeholder text
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.secondary // Light gray icon
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)) // Subtle shadow for elevation
            .background(
                color = MaterialTheme.colorScheme.surface, // Light background
                shape = RoundedCornerShape(12.dp) // Rounded corners
            ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent, // Remove underline
            unfocusedIndicatorColor = Color.Transparent, // Remove underline
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp), // Rounded corners for the TextField
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun CategoryCard(category: String, icon: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { /* Tambahkan logika filter kategori */ }
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)), // Subtle shadow for elevation
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Light background
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp) // Rounded corners
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp) // Enlarge the icon
            ) {
                icon()
            }
            // Text below the icon
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold // Bold text
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MenuCard(menu: MenuWithTenantName, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() } // Make the card clickable
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)) // Subtle shadow for elevation
            .fillMaxWidth(), // Ensure the card takes the full width of its grid cell
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Light background
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp) // Rounded corners
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for the image (since it will come from the database later)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Approximate height for the image
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)) // Placeholder background
            ) {
                // You can add a placeholder icon or text here if desired
                Text(
                    text = "Image Placeholder",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Menu name below the image
            Text(
                text = menu.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MenuItem(menu: MenuWithTenantName, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menu.name,
                    style = MaterialTheme.typography.titleLarge, // Menggunakan titleLarge (Raleway Bold, 18.sp)
                )
                Text(
                    text = menu.tenantName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Rp ${menu.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Tambah",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}