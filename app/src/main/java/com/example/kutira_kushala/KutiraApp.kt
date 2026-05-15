package com.example.kutira_kushala

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kutira_kushala.navigation.NavRoutes
import com.example.kutira_kushala.ui.screens.BootstrapScreen
import com.example.kutira_kushala.ui.screens.BusinessDetailScreen
import com.example.kutira_kushala.ui.screens.BuyerDirectoryScreen
import com.example.kutira_kushala.ui.screens.ProducerHomeScreen
import com.example.kutira_kushala.ui.screens.ProductEditorScreen
import com.example.kutira_kushala.ui.screens.ProfileEditorScreen
import com.example.kutira_kushala.ui.screens.RoleSelectScreen
import com.example.kutira_kushala.ui.screens.SignInScreen

@Composable
fun KutiraApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.BOOTSTRAP) {
        composable(NavRoutes.BOOTSTRAP) { BootstrapScreen(navController) }
        composable(NavRoutes.SIGN_IN) { SignInScreen(navController) }
        composable(NavRoutes.ROLE) { RoleSelectScreen(navController) }
        composable(NavRoutes.PRODUCER) { ProducerHomeScreen(navController) }
        composable(NavRoutes.BUYER) { BuyerDirectoryScreen(navController) }
        composable(NavRoutes.PROFILE_EDIT) { ProfileEditorScreen(navController) }
        composable(
            route = NavRoutes.PRODUCT_EDIT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) { entry ->
            val raw = entry.arguments?.getString("productId").orEmpty()
            val decoded = Uri.decode(raw)
            val id = if (decoded == "new" || decoded.isBlank()) null else decoded
            ProductEditorScreen(navController, id)
        }
        composable(
            route = NavRoutes.BUSINESS_DETAIL,
            arguments = listOf(navArgument("businessId") { type = NavType.StringType }),
        ) { entry ->
            val id = Uri.decode(entry.arguments?.getString("businessId").orEmpty())
            BusinessDetailScreen(navController, id)
        }
    }
}
