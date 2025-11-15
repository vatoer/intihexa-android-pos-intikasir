package id.stargan.intikasir.feature.home.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import id.stargan.intikasir.feature.home.ui.HomeScreen
import id.stargan.intikasir.feature.product.ui.list.ProductListScreen
import id.stargan.intikasir.feature.product.navigation.ProductRoutes
import id.stargan.intikasir.feature.settings.ui.StoreSettingsScreen
import id.stargan.intikasir.feature.pos.ui.PosScreen

/**
 * Navigation graph untuk Home feature
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    composable(HomeRoutes.HOME) {
        HomeScreen(
            onMenuClick = { route ->
                navController.navigate(route)
            },
            onLogout = onLogout
        )
    }

    // Product List Screen
    composable(HomeRoutes.PRODUCTS) {
        ProductListScreen(
            onProductClick = { productId ->
                navController.navigate(ProductRoutes.productDetail(productId))
            },
            onAddProductClick = {
                navController.navigate(ProductRoutes.PRODUCT_ADD)
            },
            onManageCategoriesClick = {
                navController.navigate(ProductRoutes.CATEGORY_MANAGE)
            },
            onBackClick = { navController.navigateUp() }
        )
    }

    // Product Detail Screen (Placeholder)
    composable(ProductRoutes.PRODUCT_DETAIL) {
        PlaceholderScreen(title = "Detail Produk", onBack = { navController.navigateUp() })
    }

    // Product Add Screen (Placeholder)
    composable(ProductRoutes.PRODUCT_ADD) {
        PlaceholderScreen(title = "Tambah Produk", onBack = { navController.navigateUp() })
    }

    // Product Edit Screen (Placeholder)
    composable(ProductRoutes.PRODUCT_EDIT) {
        PlaceholderScreen(title = "Edit Produk", onBack = { navController.navigateUp() })
    }

    // Category Management Screen (Placeholder)
    composable(ProductRoutes.CATEGORY_MANAGE) {
        PlaceholderScreen(title = "Kelola Kategori", onBack = { navController.navigateUp() })
    }

    composable(HomeRoutes.HISTORY) {
        PlaceholderScreen(title = "Riwayat Transaksi", onBack = { navController.navigateUp() })
    }

    composable(HomeRoutes.EXPENSES) {
        PlaceholderScreen(title = "Pengeluaran", onBack = { navController.navigateUp() })
    }

    composable(HomeRoutes.REPORTS) {
        PlaceholderScreen(title = "Laporan", onBack = { navController.navigateUp() })
    }

    composable(HomeRoutes.PRINT_RECEIPT) {
        PlaceholderScreen(title = "Cetak Resi", onBack = { navController.navigateUp() })
    }

    // POS Screen (Kasir)
    composable(HomeRoutes.CASHIER) {
        PosScreen(
            onPay = { total ->
                // Navigate back to home after payment
                navController.navigateUp()
            }
        )
    }

    // Store Settings Screen
    composable(HomeRoutes.SETTINGS) {
        StoreSettingsScreen(
            onNavigateBack = { navController.navigateUp() }
        )
    }
}

/**
 * Placeholder screen untuk menu yang belum diimplementasi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Halaman $title\n(Belum diimplementasi)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
