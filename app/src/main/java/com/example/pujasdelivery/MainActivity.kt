package com.example.pujasdelivery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pujasdelivery.ui.CategoryScreen
import com.example.pujasdelivery.ui.DashboardScreen
import com.example.pujasdelivery.ui.theme.PujasDeliveryTheme
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import com.example.pujasdelivery.ui.screens.TenantDescScreen
import com.example.pujasdelivery.ui.screens.ProfileScreen
import com.example.pujasdelivery.ui.screens.EditProfileScreen
import com.example.pujasdelivery.ui.screens.OrdersScreen
import com.example.pujasdelivery.ui.screens.MenuDetailScreen
import com.example.pujasdelivery.ui.screens.CheckoutScreen
import com.example.pujasdelivery.ui.screens.PaymentScreen
import com.example.pujasdelivery.ui.screens.OrderConfirmationScreen
import com.example.pujasdelivery.ui.courier.CourierOrderScreen
import com.example.pujasdelivery.ui.courier.ProfileCourierScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val role = intent.getStringExtra("role")
        val startDestination = when (role) {
            "kurir" -> "courier_orders"
            else -> "dashboard" // Default ke DashboardScreen untuk pengguna
        }
        Log.d("MainActivity", "Starting with role: $role, destination: $startDestination")

        setContent {
            val navController = rememberNavController()
            PujasDeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationSetup(
                        navController = navController,
                        viewModel = viewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationSetup(
    navController: NavHostController,
    viewModel: DashboardViewModel,
    startDestination: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null) {
                when {
                    currentRoute.startsWith("courier_orders") || currentRoute.startsWith("courier_profile") -> {
                        CourierBottomNavigationBar(navController)
                    }
                    !currentRoute.startsWith("checkout") &&
                            !currentRoute.startsWith("payment") &&
                            !currentRoute.startsWith("orderConfirmation") &&
                            !currentRoute.startsWith("orderDetail") -> {
                        BottomNavigationBar(navController)
                    }
                    else -> Unit
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel, navController)
            }
            composable("orders") {
                OrdersScreen(navController, viewModel)
            }
            composable("profile") {
                ProfileScreen(navController)
            }
            composable("editProfile") {
                EditProfileScreen(navController)
            }
            composable("category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: "Makanan"
                CategoryScreen(category = category, viewModel, navController)
            }
            composable("menuDetail/{menuName}") { backStackEntry ->
                val menuName = backStackEntry.arguments?.getString("menuName") ?: ""
                MenuDetailScreen(
                    menuName = menuName,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(
                route = "payment?gedungId={gedungId}",
                arguments = listOf(navArgument("gedungId") { type = NavType.LongType })
            ) { backStackEntry ->
                val gedungId = backStackEntry.arguments?.getLong("gedungId")
                PaymentScreen(
                    navController = navController,
                    viewModel = viewModel,
                    gedungId = gedungId
                )
            }
            composable(
                route = "orderConfirmation/{orderId}?cancel={cancel}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("cancel") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: "1"
                val cancel = backStackEntry.arguments?.getBoolean("cancel") ?: false
                OrderConfirmationScreen(
                    navController = navController,
                    viewModel = viewModel,
                    orderId = orderId
                )
                if (cancel) {
                    Log.d("Cancellation", "Order $orderId cancelled")
                }
            }
            composable("tenantDesc/{tenantName}/{tenantId}") { backStackEntry ->
                val tenantName = backStackEntry.arguments?.getString("tenantName")
                val tenantId = backStackEntry.arguments?.getString("tenantId")?.toIntOrNull()
                Log.d("Navigation", "Navigating to tenantDesc with tenantName: $tenantName, tenantId: $tenantId")
                TenantDescScreen(
                    tenantName = tenantName,
                    tenantId = tenantId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("courier_orders") {
                CourierOrderScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("courier_profile") {
                ProfileCourierScreen(navController = navController)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
        items.forEach { (title, icon) ->
            val route = when (title) {
                "Beranda" -> "dashboard"
                "Pesanan" -> "orders"
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

@Composable
fun CourierBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        "Pesanan" to Icons.Default.ShoppingCart,
        "Profil" to Icons.Default.Person
    )

    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(16.dp)),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        items.forEach { (title, icon) ->
            val route = when (title) {
                "Pesanan" -> "courier_orders"
                "Profil" -> "courier_profile"
                else -> "courier_orders"
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