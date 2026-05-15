package com.example.kutira_kushala.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kutira_kushala.R
import com.example.kutira_kushala.data.model.UserRole
import com.example.kutira_kushala.data.repo.UserRepository
import com.example.kutira_kushala.navigation.NavRoutes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun BootstrapScreen(navController: NavHostController) {
    var message by remember { mutableStateOf<String?>(null) }
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        
        // Add a slight delay for splash feel
        delay(1500)

        if (user == null) {
            navController.navigate(NavRoutes.SIGN_IN) {
                popUpTo(NavRoutes.BOOTSTRAP) { inclusive = true }
            }
            return@LaunchedEffect
        }
        val roleStr = try {
            UserRepository().getRole(user.uid)
        } catch (_: Exception) {
            message = "Could not reach your profile. Choose a role to continue."
            null
        }
        val dest = when (UserRole.fromFirestore(roleStr)) {
            null -> NavRoutes.ROLE
            UserRole.PRODUCER -> NavRoutes.PRODUCER
            UserRole.BUYER -> NavRoutes.BUYER
        }
        navController.navigate(dest) {
            popUpTo(NavRoutes.BOOTSTRAP) { inclusive = true }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kklogo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Kutira Kushala",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "Empowering Micro-Factories",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(48.dp))
                if (message == null) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = message!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}
