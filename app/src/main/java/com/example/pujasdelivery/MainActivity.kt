package com.example.pujasdelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.pujasdelivery.ui.CategoryScreen
import com.example.pujasdelivery.ui.DashboardScreen
import com.example.pujasdelivery.ui.screens.TenantDescScreen
import com.example.pujasdelivery.ui.screens.MenuDetailScreen
import com.example.pujasdelivery.ui.screens.CheckoutScreen
import com.example.pujasdelivery.ui.screens.OrderConfirmationScreen
import com.example.pujasdelivery.ui.screens.OrdersScreen
import com.example.pujasdelivery.ui.screens.ProfileScreen
import com.example.pujasdelivery.ui.theme.PujasDeliveryTheme
import com.example.pujasdelivery.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PujasDeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationSetup(navController = rememberNavController(), viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NavigationSetup(navController: NavHostController, viewModel: DashboardViewModel) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && !currentRoute.startsWith("checkout") &&
                !currentRoute.startsWith("orderConfirmation")
            ) {
                BottomNavigationBar(navController)
            }
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
            composable("tenantDesc/{tenantName}/{tenantId}") { backStackEntry ->
                val tenantName = backStackEntry.arguments?.getString("tenantName")
                val tenantId = backStackEntry.arguments?.getString("tenantId")?.toIntOrNull()
                TenantDescScreen(
                    tenantName = tenantName,
                    tenantId = tenantId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("menuDetail/{menuName}") { backStackEntry ->
                val menuName = backStackEntry.arguments?.getString("menuName") ?: ""
                MenuDetailScreen(
                    menuName = menuName,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                CategoryScreen(
                    category = category,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("orders") {
                OrdersScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("checkout") {
                CheckoutScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("orderConfirmation/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
                OrderConfirmationScreen(
                    orderId = orderId,
                    navController = navController
                )
            }
            composable("editProfile") {
                // Tambahkan EditProfileScreen jika diperlukan
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        "Beranda" to Icons.Default.Home,
        "Pesanan" to Icons.Default.ShoppingCart,
        "Profil" to Icons.Default.Person
    )

    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { (title, icon) ->
            val route = when (title) {
                "Beranda" -> "dashboard"
                "Pesanan" -> "orders"
                "Profil" -> "profile"
                else -> "dashboard"
            }
            val isSelected = when (title) {
                "Beranda" -> currentRoute == "dashboard" ||
                        currentRoute?.startsWith("category") == true ||
                        currentRoute?.startsWith("tenantDesc") == true ||
                        currentRoute?.startsWith("menuDetail") == true
                else -> currentRoute == route
            }
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
                    if (title == "Beranda" && (currentRoute == "orders" || currentRoute == "profile")) {
                        // Jika berpindah dari Pesanan atau Profil ke Beranda, reset ke dashboard
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        // Navigasi normal untuk tab lain atau saat sudah di Beranda
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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