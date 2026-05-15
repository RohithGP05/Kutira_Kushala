package com.example.kutira_kushala.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kutira_kushala.data.model.UserRole
import com.example.kutira_kushala.data.repo.BusinessRepository
import com.example.kutira_kushala.data.repo.UserRepository
import com.example.kutira_kushala.navigation.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RoleSelectScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val userRepo = remember { UserRepository() }
    val businessRepo = remember { BusinessRepository() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var message by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Welcome to Kutira Kushala",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Choose how you want to use the platform to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        RoleCard(
            title = "I am a Producer",
            description = "Showcase your micro-factory, upload product catalog, and manage your business profile.",
            icon = Icons.Outlined.Palette,
            buttonText = "Start as Producer",
            color = MaterialTheme.colorScheme.primary,
            isLoading = isProcessing,
            onClick = {
                if (uid == null || isProcessing) return@RoleCard
                scope.launch {
                    try {
                        isProcessing = true
                        userRepo.setRole(uid, UserRole.PRODUCER.firestoreValue)
                        businessRepo.ensureBusinessStub(uid)
                        navController.navigate(NavRoutes.PRODUCER) {
                            popUpTo(NavRoutes.ROLE) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        message = e.message ?: "Error saving role."
                    } finally {
                        isProcessing = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleCard(
            title = "I am a Buyer",
            description = "Explore verified producers, browse wholesale catalogs, and connect directly with micro-factories.",
            icon = Icons.Outlined.Storefront,
            buttonText = "Start as Buyer",
            color = MaterialTheme.colorScheme.tertiary,
            isLoading = isProcessing,
            onClick = {
                if (uid == null || isProcessing) return@RoleCard
                scope.launch {
                    try {
                        isProcessing = true
                        userRepo.setRole(uid, UserRole.BUYER.firestoreValue)
                        navController.navigate(NavRoutes.BUYER) {
                            popUpTo(NavRoutes.ROLE) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        message = e.message ?: "Error saving role."
                    } finally {
                        isProcessing = false
                    }
                }
            }
        )

        message?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    color: androidx.compose.ui.graphics.Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                enabled = !isLoading
            ) {
                Text(buttonText)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
