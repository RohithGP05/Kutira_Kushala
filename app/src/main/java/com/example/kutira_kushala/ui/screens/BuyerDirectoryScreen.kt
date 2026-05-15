package com.example.kutira_kushala.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.kutira_kushala.navigation.NavRoutes
import com.example.kutira_kushala.ui.components.*
import com.example.kutira_kushala.ui.viewmodel.BuyerViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDirectoryScreen(navController: NavHostController) {
    val vm: BuyerViewModel = viewModel()
    val businesses by vm.businesses.collectAsState()
    val filter by vm.directoryFilter.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()
    var search by remember { mutableStateOf("") }

    val filtered = remember(businesses, search) {
        if (search.isBlank()) businesses
        else businesses.filter { b ->
            listOf(b.businessName, b.skillArea, b.locationText, b.district, b.state)
                .any { it.contains(search, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Discover Producers",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Empowering local micro-factories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(NavRoutes.SIGN_IN) {
                            popUpTo(NavRoutes.BUYER) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Search Bar
                SearchBarArea(search) { search = it }

                // Categories
                CategoryChips(selectedCategory = filter.category) { vm.setCategory(it) }

                // Filter Row
                FilterOptionsRow(
                    onlyAccepting = filter.onlyAcceptingOrders,
                    onToggle = { vm.setOnlyAcceptingOrders(it) }
                )

                if (filtered.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "No Producers Found",
                        description = "Try adjusting your search or category filters to find more micro-factories.",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filtered, key = { it.id }) { b ->
                            BuyerBusinessCard(b) {
                                navController.navigate(NavRoutes.businessDetail(b.id))
                            }
                        }
                    }
                }
            }
        }
    }
}
