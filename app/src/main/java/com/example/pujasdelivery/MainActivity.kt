    package com.example.pujasdelivery

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.viewModels
    import androidx.compose.foundation.background
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.layout.*
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Home
    import androidx.compose.material.icons.filled.Message
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.material.icons.filled.ShoppingCart
    import androidx.compose.material3.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.livedata.observeAsState
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.draw.shadow
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.currentBackStackEntryAsState
    import androidx.navigation.compose.rememberNavController
    import com.example.pujasdelivery.ui.DashboardScreen
    import com.example.pujasdelivery.ui.theme.*
    import com.example.pujasdelivery.viewmodel.DashboardViewModel
    import com.example.pujasdelivery.ui.screens.TenantDescScreen
    import com.example.pujasdelivery.data.Tenant
    import androidx.compose.runtime.livedata.observeAsState


    class MainActivity : ComponentActivity() {
        private val viewModel: DashboardViewModel by viewModels()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                val navController = rememberNavController()
                PujasDeliveryTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavigationSetup(navController, viewModel)
                    }
                }
            }
        }
    }

    @Composable
    fun NavigationSetup(navController: NavHostController, viewModel: DashboardViewModel) {
        val tenants by viewModel.tenants.observeAsState(initial = emptyList())
        val menus by viewModel.menus.observeAsState(initial = emptyList()) // Tambahan ini penting

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
                    DashboardScreen(viewModel, navController)
                }
                composable("orders") {
                    Text("Orders Screen", modifier = Modifier.padding(16.dp))
                }
                composable("chat") {
                    Text("Chat Screen", modifier = Modifier.padding(16.dp))
                }
                composable("profile") {
                    Text("Profile Screen", modifier = Modifier.padding(16.dp))
                }
                composable("menuDetail/{menuName}") { backStackEntry ->
                    val menuName = backStackEntry.arguments?.getString("menuName") ?: ""
                    Text("Menu Detail Screen for $menuName", modifier = Modifier.padding(16.dp))
                }
                composable("tenantDesc/{tenantName}") { backStackEntry ->
                    val tenantName = backStackEntry.arguments?.getString("tenantName")
                    TenantDescScreen(
                        tenantName = tenantName,
                        navController = navController,
                        tenants = tenants,
                        menus = menus // disediakan di sini
                    )
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            "Beranda" to Icons.Default.Home,
            "Pesanan" to Icons.Default.ShoppingCart,
            "Chat" to Icons.Filled.Message,
            "Profil" to Icons.Default.Person
        )

        NavigationBar(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp) // Spacing from sides and bottom
                .shadow(4.dp, shape = RoundedCornerShape(16.dp)) // Shadow for floating effect
                .clip(RoundedCornerShape(16.dp)), // Rounded corners
            containerColor = MaterialTheme.colorScheme.primary, // Background navigasi: PrimaryDarkBlue
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            items.forEach { (title, icon) ->
                val route = when (title) {
                    "Beranda" -> "dashboard"
                    "Pesanan" -> "orders"
                    "Chat" -> "chat"
                    "Profil" -> "profile"
                    else -> "dashboard"
                }
                val isSelected = currentRoute == route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    },
                    label = {
                        Text(
                            text = title,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = if (isSelected) {
                        Modifier.background(MaterialTheme.colorScheme.background)
                    } else {
                        Modifier
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }