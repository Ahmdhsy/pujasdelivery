package com.example.pujasdelivery.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.pujasdelivery.R
import com.example.pujasdelivery.data.MenuWithTenantName
import com.example.pujasdelivery.data.Tenant
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDescScreen(
    tenantName: String?,
    tenantId: Int?,
    navController: NavHostController,
    viewModel: DashboardViewModel
) {
    // Tambahkan penanganan jika tenantId null
    if (tenantId == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E5E5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ID Tenant tidak valid",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    // Pastikan data dimuat untuk tenant yang dipilih
    LaunchedEffect(tenantId) {
        viewModel.loadMenusForTenant(tenantId)
    }

    val tenants by viewModel.tenants.observeAsState(initial = emptyList())
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val tenant = tenants.find { tenant -> tenant.id == tenantId } ?: Tenant(id = tenantId, name = tenantName ?: "Unknown Tenant")
    val tenantMenus = menus.filter { menu -> menu.tenantId == tenantId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Tenant",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (tenantMenus.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFE5E5E5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada menu tersedia",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            TenantDetailContent(
                tenant = tenant,
                menus = tenantMenus,
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun TenantDetailContent(
    tenant: Tenant,
    menus: List<MenuWithTenantName>,
    navController: NavHostController,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val totalItemCount by viewModel.totalItemCount.observeAsState(initial = 0)
    val totalPrice by viewModel.totalPrice.observeAsState(initial = 0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE5E5E5))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 72.dp), // Add padding to avoid overlap with floating footer
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Tenant Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = tenant.imageURL,
                        placeholder = painterResource(R.drawable.pujas),
                        error = painterResource(R.drawable.pujas)
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Gambar Tenant",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Tenant Name
                Text(
                    text = tenant.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            item {
                // Tenant Description
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deskripsi",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tenant.description ?: "Deskripsi belum tersedia",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            item {
                // Judul Menu
                Text(
                    text = "Menu Tersedia",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(menus) { menu ->
                MenuItemCard(
                    menu = menu,
                    viewModel = viewModel,
                    onAddClick = { viewModel.addToCart(menu) },
                    onRemoveClick = { viewModel.removeFromCart(menu) }
                )
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
fun MenuItemCard(
    menu: MenuWithTenantName,
    viewModel: DashboardViewModel,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    // Observe the cart items to get the quantity of this specific menu item
    val cartItems by viewModel.cartItems.observeAsState(initial = emptyList())
    val itemCount = cartItems.find { it.menuId == menu.id && it.tenantName == menu.tenantName }?.quantity ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = menu.tenantName ?: "Unknown Tenant",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                            onClick = { onRemoveClick() },
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
                            onClick = { onAddClick() },
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
                        onClick = { onAddClick() },
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