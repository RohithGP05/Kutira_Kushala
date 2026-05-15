package com.example.kutira_kushala.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.kutira_kushala.data.repo.BusinessRepository
import com.example.kutira_kushala.ui.components.ProductDetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(navController: NavHostController, businessId: String) {
    val context = LocalContext.current
    val repo = remember { BusinessRepository() }
    val profile by repo.observeBusiness(businessId).collectAsState(initial = null)
    val products by repo.observeProducts(businessId).collectAsState(initial = emptyList())

    fun dial() {
        val raw = profile?.contactPhone?.trim().orEmpty()
        if (raw.isEmpty()) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(raw)}"))
        context.startActivity(intent)
    }

    fun whatsapp() {
        val raw = profile?.whatsappNumber?.trim().orEmpty().filter { it.isDigit() }
        if (raw.isEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$raw"))
        context.startActivity(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            profile?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Row(
                        Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { dial() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Call Producer")
                        }
                        Button(
                            onClick = { whatsapp() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("WhatsApp")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            item {
                Box {
                    AsyncImage(
                        model = profile?.teamPhotoUrl?.takeIf { it.isNotBlank() },
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop,
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            }

            item {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = profile?.businessName.orEmpty().ifBlank { "Micro-Factory" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = profile?.skillArea.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = listOfNotNull(
                                profile?.locationText?.takeIf { it.isNotBlank() },
                                profile?.district?.takeIf { it.isNotBlank() },
                                profile?.state?.takeIf { it.isNotBlank() },
                            ).joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Production Capacity",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                profile?.dailyCapacityText.orEmpty().ifBlank { "Contact for capacity details" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (profile?.weeklyCapacityNote?.isNotBlank() == true) {
                                Text(
                                    profile?.weeklyCapacityNote.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            val statusText = if (profile?.acceptingOrders == true) "Accepting Orders" else "Currently Unavailable"
                            val statusColor = if (profile?.acceptingOrders == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            
                            Surface(
                                color = statusColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ) {
                                Text(
                                    statusText,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    Text(
                        "Product Catalog",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No products listed yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(products, key = { it.id }) { p ->
                    ProductDetailCard(p)
                }
            }
            
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
