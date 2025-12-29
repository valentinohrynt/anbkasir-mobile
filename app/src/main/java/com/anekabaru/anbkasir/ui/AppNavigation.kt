package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anekabaru.anbkasir.ui.admin.InventoryScreen
import com.anekabaru.anbkasir.ui.admin.ReportScreen
import com.anekabaru.anbkasir.ui.login.LoginScreen
import com.anekabaru.anbkasir.ui.pos.CartScreen
import com.anekabaru.anbkasir.ui.pos.PosScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val POS = "pos"
    const val CART = "cart" // New Route
    const val INVENTORY = "inventory"
    const val REPORTS = "reports"
}

@Composable
fun AppNavigation(
    viewModel: PosViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                role = viewModel.currentUserRole,
                onNav = { route -> navController.navigate(route) },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // POS (Catalog) Screen
        composable(Routes.POS) {
            PosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onViewCart = { navController.navigate(Routes.CART) } // Navigate to Cart
            )
        }

        // NEW: Cart Screen
        composable(Routes.CART) {
            CartScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INVENTORY) {
            InventoryScreen(viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.REPORTS) {
            ReportScreen(viewModel, onBack = { navController.popBackStack() })
        }
    }
}