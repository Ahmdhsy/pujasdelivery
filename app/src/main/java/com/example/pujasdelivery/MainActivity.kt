package com.example.pujasdelivery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pujasdelivery.ui.ChatScreen
import com.example.pujasdelivery.ui.theme.PujasDeliveryTheme
import com.example.pujasdelivery.viewmodel.ChatViewModel
import com.example.pujasdelivery.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PujasDeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavigationSetup(navController, application)
                }
            }
        }
    }
}

@Composable
fun NavigationSetup(navController: NavHostController, application: android.app.Application) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                Log.d("Navigation", "Navigating to DashboardScreen")
                val dashboardViewModel: DashboardViewModel = viewModel()
                DashboardScreen(dashboardViewModel)
            }
            composable("orders") {
                Log.d("Navigation", "Navigating to OrdersScreen")
                Text("Layar Pesanan", modifier = Modifier.padding(16.dp))
            }
            composable("chat") {
                Log.d("Navigation", "Navigating to ChatListScreen")
                ChatListScreen(navController)
            }
            composable("chat_detail/{tenantId}") { backStackEntry ->
                // State untuk menyimpan tenantId dan error
                var tenantId by remember { mutableStateOf<Int?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Inisialisasi tenantId menggunakan LaunchedEffect
                LaunchedEffect(backStackEntry) {
                    try {
                        val id = backStackEntry.arguments?.getString("tenantId")?.toIntOrNull() ?: 1
                        Log.d("Navigation", "Navigating to ChatScreen with tenantId: $id")
                        tenantId = id
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error parsing tenantId: ${e.message}", e)
                        errorMessage = "Gagal memuat tenantId: ${e.message}"
                    }
                }

                // Tampilkan UI berdasarkan state
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (tenantId != null) {
                    val factory = ChatViewModelFactory(
                        userId = 1, // Asumsikan userId = 1
                        tenantId = tenantId!!,
                        application = application
                    )
                    val chatViewModel: ChatViewModel = viewModel(factory = factory)
                    ChatScreen(chatViewModel)
                } else {
                    // Tampilkan loading sementara tenantId belum diinisialisasi
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center // Gunakan contentAlignment untuk menyelaraskan
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            composable("profile") {
                Log.d("Navigation", "Navigating to ProfileScreen")
                Text("Layar Profil", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "Beranda" to Icons.Default.Home,
        "Pesanan" to Icons.Default.ShoppingCart,
        "Chat" to Icons.Default.Message,
        "Profil" to Icons.Default.Person
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF1E3A8A) // Warna biru tua sesuai Figma
    ) {
        items.forEach { (title, icon) ->
            val route = when (title) {
                "Beranda" -> "dashboard"
                "Pesanan" -> "orders"
                "Chat" -> "chat"
                "Profil" -> "profile"
                else -> "dashboard"
            }
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title, tint = Color.White) },
                label = { Text(title, color = Color.White, fontSize = 12.sp) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val tenants by viewModel.tenants.observeAsState(initial = emptyList())
    val menus by viewModel.menus.observeAsState(initial = emptyList())
    val loadingState by viewModel.loadingState.observeAsState(initial = DashboardViewModel.LoadingState.Idle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Beranda",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (loadingState) {
            DashboardViewModel.LoadingState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            DashboardViewModel.LoadingState.Success -> {
                // Tampilkan daftar tenant
                Text(
                    text = "Daftar Tenant",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyColumn {
                    items(tenants) { tenant ->
                        TenantItem(
                            tenant = tenant,
                            onClick = { viewModel.loadMenusForTenant(tenant.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tampilkan daftar menu untuk tenant yang dipilih
                if (menus.isNotEmpty()) {
                    Text(
                        text = "Menu dari ${menus.first().tenantName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyColumn {
                        items(menus) { menu ->
                            MenuItem(menu = menu)
                        }
                    }
                }
            }
            DashboardViewModel.LoadingState.Error -> {
                Text(
                    text = "Gagal memuat data",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {}
        }
    }
}

@Composable
fun TenantItem(tenant: com.example.pujasdelivery.data.Tenant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = "Tenant Icon",
                modifier = Modifier.size(40.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = tenant.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun MenuItem(menu: com.example.pujasdelivery.data.MenuWithTenantName) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = menu.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Harga: Rp ${menu.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = menu.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ChatListScreen(navController: NavHostController) {
    // Data dummy untuk daftar chat
    val chatList = listOf(
        ChatSummary("Nama Kurir", "Saya sudah di depan Gedung H", "12:47", 1, 1),
        ChatSummary("Warung 01", "Es tehnya habis, mau ganti apa?", "12:20", 1, 2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chat",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(chatList) { chat ->
                ChatListItem(
                    name = chat.name,
                    message = chat.message,
                    time = chat.time,
                    unreadCount = chat.unreadCount,
                    onClick = {
                        navController.navigate("chat_detail/${chat.tenantId}")
                    }
                )
            }
        }
    }
}

data class ChatSummary(
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int,
    val tenantId: Int
)

@Composable
fun ChatListItem(
    name: String,
    message: String,
    time: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Icon",
            modifier = Modifier.size(48.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFA500), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Factory untuk ChatViewModel
class ChatViewModelFactory(
    private val userId: Int,
    private val tenantId: Int,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                application = application,
                userId = userId,
                tenantId = tenantId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}