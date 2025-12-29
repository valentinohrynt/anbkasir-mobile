package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anekabaru.anbkasir.ui.admin.*
import com.anekabaru.anbkasir.ui.login.LoginScreen
import com.anekabaru.anbkasir.ui.pos.CartScreen
import com.anekabaru.anbkasir.ui.pos.PosScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val POS = "pos"
    const val CART = "cart"
    const val INVENTORY = "inventory"
    const val PRODUCT_DETAIL = "product_detail"
    const val PRODUCT_FORM = "product_form"
    const val REPORTS = "reports"
    const val HISTORY = "history"
    const val TRANSACTION_DETAIL = "transaction_detail"
}

@Composable
fun AppNavigation(
    viewModel: PosViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            // FIXED: Used named arguments to fix the type mismatch error
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
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.POS) {
            PosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onViewCart = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            CartScreen(viewModel, onBack = { navController.popBackStack() })
        }

        // --- INVENTORY FLOW (Updated for Navigation) ---

        // 1. LIST
        composable(Routes.INVENTORY) {
            InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(Routes.PRODUCT_DETAIL) },
                onNavigateToForm = { navController.navigate(Routes.PRODUCT_FORM) }
            )
        }

        // 2. DETAIL
        composable(Routes.PRODUCT_DETAIL) {
            ProductDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.PRODUCT_FORM) }
            )
        }

        // 3. FORM (Add / Edit)
        composable(Routes.PRODUCT_FORM) {
            ProductFormScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REPORTS) {
            ReportScreen(viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            SalesHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(Routes.TRANSACTION_DETAIL) } // Navigasi ke detail
            )
        }

        // 2. TRANSACTION DETAIL SCREEN (New)
        composable(Routes.TRANSACTION_DETAIL) {
            TransactionDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}