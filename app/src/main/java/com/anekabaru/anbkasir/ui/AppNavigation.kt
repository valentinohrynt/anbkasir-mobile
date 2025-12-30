package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anekabaru.anbkasir.ui.admin.InventoryScreen
import com.anekabaru.anbkasir.ui.admin.ProductDetailScreen
import com.anekabaru.anbkasir.ui.admin.ProductFormScreen
import com.anekabaru.anbkasir.ui.admin.ReportScreen
import com.anekabaru.anbkasir.ui.admin.SalesHistoryScreen
import com.anekabaru.anbkasir.ui.admin.TransactionDetailScreen
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
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // --- PERBAIKAN DI SINI ---
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToPos = { navController.navigate(Routes.POS) },
                onNavigateToInventory = { navController.navigate(Routes.INVENTORY) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToReport = { navController.navigate(Routes.REPORTS) }
            )
        }
        // --------------------------

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

        // --- INVENTORY FLOW ---

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
                onNavigateToDetail = { navController.navigate(Routes.TRANSACTION_DETAIL) }
            )
        }

        // 2. TRANSACTION DETAIL SCREEN
        composable(Routes.TRANSACTION_DETAIL) {
            TransactionDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}