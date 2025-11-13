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

    // Placeholder screens untuk setiap menu
    composable(HomeRoutes.PRODUCTS) {
        PlaceholderScreen(title = "Produk", onBack = { navController.navigateUp() })
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

    composable(HomeRoutes.CASHIER) {
        PlaceholderScreen(title = "Kasir", onBack = { navController.navigateUp() })
    }

    composable(HomeRoutes.SETTINGS) {
        PlaceholderScreen(title = "Pengaturan", onBack = { navController.navigateUp() })
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

