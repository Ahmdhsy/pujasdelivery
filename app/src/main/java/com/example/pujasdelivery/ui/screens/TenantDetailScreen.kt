package com.example.pujasdelivery.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pujasdelivery.viewmodel.DashboardViewModel

@Composable
fun TenantDescScreen(
    tenantName: String?,
    tenantId: Int?,
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val menus by viewModel.menus.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = tenantName ?: "Tenant",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (menus.isEmpty()) {
            Text(
                text = "Tidak ada menu tersedia",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menus.size) { index ->
                    MenuCard(
                        menu = menus[index],
                        onClick = {
                            navController.navigate("menuDetail/${menus[index].name}")
                        }
                    )
                }
            }
        }
    }
}