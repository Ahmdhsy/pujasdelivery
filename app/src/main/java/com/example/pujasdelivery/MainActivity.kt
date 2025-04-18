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
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.pujasdelivery.ui.DashboardScreen
import com.example.pujasdelivery.ui.screens.LoginScreen
import com.example.pujasdelivery.ui.screens.RegisterScreen
import com.example.pujasdelivery.ui.screens.TenantDescScreen
import com.example.pujasdelivery.ui.theme.PujasDeliveryTheme
import com.example.pujasdelivery.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            val navController = rememberNavController()

            // Mengecek apakah user sudah login
            val isLoggedIn = auth.currentUser != null
            val startDestination = if (isLoggedIn) "dashboard" else "login"

            PujasDeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(navController, viewModel, startDestination)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: DashboardViewModel,
    startDestination: String
) {
    val tenants by viewModel.tenants.observeAsState(initial = emptyList())
    val menus by viewModel.menus.observeAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                navToHome = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } },
                navToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                navToLogin = { navController.navigate("login") { popUpTo("register") { inclusive = true } } }
            )
        }
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
                menus = menus
            )
        }
    }

    // Bottom navigation hanya tampil kalau sudah login (di dashboard dan rute terkait)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    if (currentRoute in listOf("dashboard", "orders", "chat", "profile")) {
        BottomNavigationBar(navController)
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
