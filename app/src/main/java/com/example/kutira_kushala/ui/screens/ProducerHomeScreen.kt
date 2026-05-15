package com.example.kutira_kushala.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kutira_kushala.data.repo.BusinessRepository
import com.example.kutira_kushala.navigation.NavRoutes
import com.example.kutira_kushala.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerHomeScreen(navController: NavHostController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val scope = rememberCoroutineScope()
    val repo = remember { BusinessRepository() }
    val profile by repo.observeBusiness(uid).collectAsState(initial = null)
    val products by repo.observeProducts(uid).collectAsState(initial = emptyList())

    var daily by remember { mutableStateOf("") }
    var weekly by remember { mutableStateOf("") }
    var accepting by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        runCatching { repo.ensureBusinessStub(uid) }
    }

    LaunchedEffect(products.map { it.category }) {
        if (products.isNotEmpty()) {
            runCatching { repo.syncCategories(uid) }
        }
    }

    LaunchedEffect(profile?.dailyCapacityText, profile?.weeklyCapacityNote, profile?.acceptingOrders) {
        profile?.let {
            daily = it.dailyCapacityText
            weekly = it.weeklyCapacityNote
            accepting = it.acceptingOrders
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Producer Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(NavRoutes.SIGN_IN) {
                            popUpTo(NavRoutes.PRODUCER) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
        floatingActionButton = {
            if (products.size < BusinessRepository.MAX_PRODUCTS) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.productEdit("new")) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Product") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                )
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Profile Card
                item {
                    ProducerProfileCard(profile) {
                        navController.navigate(NavRoutes.PROFILE_EDIT)
                    }
                }

                // Capacity Meter Card
                item {
                    CapacityCard(
                        accepting = accepting,
                        daily = daily,
                        weekly = weekly,
                        onDailyChange = { daily = it },
                        onWeeklyChange = { weekly = it },
                        onAcceptingChange = { v ->
                            accepting = v
                            scope.launch { repo.updateCapacity(uid, v, daily, weekly) }
                        },
                        onSave = {
                            scope.launch { repo.updateCapacity(uid, accepting, daily, weekly) }
                        }
                    )
                }

                // Products Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "My Catalog",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "${products.size}/${BusinessRepository.MAX_PRODUCTS}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (products.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.Inventory2,
                            title = "No products yet",
                            description = "Start adding items to your catalog to reach more buyers."
                        )
                    }
                } else {
                    items(products, key = { it.id }) { p ->
                        ProducerProductCard(p) {
                            navController.navigate(NavRoutes.productEdit(p.id))
                        }
                    }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
