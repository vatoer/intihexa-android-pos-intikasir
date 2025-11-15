package id.stargan.intikasir.feature.home.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.feature.home.ui.HomeScreen
import id.stargan.intikasir.feature.product.ui.list.ProductListScreen
import id.stargan.intikasir.feature.product.navigation.ProductRoutes
import id.stargan.intikasir.feature.settings.ui.StoreSettingsScreen
import id.stargan.intikasir.feature.settings.ui.StoreSettingsViewModel
import id.stargan.intikasir.feature.pos.ui.PosScreenReactive
import id.stargan.intikasir.feature.pos.ui.cart.CartScreenReactive
import id.stargan.intikasir.feature.pos.ui.payment.PaymentScreenReactive
import id.stargan.intikasir.feature.pos.ui.receipt.ReceiptScreen
import id.stargan.intikasir.feature.pos.navigation.PosRoutes
import id.stargan.intikasir.feature.pos.print.ReceiptPrinter
import kotlinx.coroutines.launch

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
                if (route == "cashier") {
                    // Navigate to cashier route with optional transactionId param omitted
                    navController.navigate(PosRoutes.POS)
                } else {
                    navController.navigate(route)
                }
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

    // POS Screen (Kasir) - Reactive version
    composable(
        route = PosRoutes.POS_WITH_ID,
        arguments = listOf(
            navArgument("transactionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")

        PosScreenReactive(
            transactionId = transactionId,
            onNavigateToCart = { txId ->
                navController.navigate(PosRoutes.cart(txId))
            },
            onNavigateToPayment = { txId ->
                navController.navigate(PosRoutes.payment(txId))
            },
            onNavigateBack = { navController.navigateUp() }
        )
    }

    // Cart Screen - Reactive version
    composable(
        route = PosRoutes.CART,
        arguments = listOf(
            navArgument("transactionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")!!

        CartScreenReactive(
            transactionId = transactionId,
            onNavigateBack = { navController.navigateUp() },
            onNavigateToPayment = { txId ->
                navController.navigate(PosRoutes.payment(txId))
            }
        )
    }

    // Payment Screen - Reactive version
    composable(
        route = PosRoutes.PAYMENT,
        arguments = listOf(
            navArgument("transactionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")!!

        PaymentScreenReactive(
            transactionId = transactionId,
            onPaymentSuccess = { txId ->
                navController.navigate(PosRoutes.receipt(txId)) {
                    popUpTo(HomeRoutes.HOME) { inclusive = false }
                }
            },
            onNavigateBack = { navController.navigateUp() }
        )
    }

    // Receipt Screen
    composable(
        route = PosRoutes.RECEIPT,
        arguments = listOf(
            navArgument("transactionId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val transactionId = backStackEntry.arguments?.getString("transactionId")!!
        val viewModel = hiltViewModel<id.stargan.intikasir.feature.pos.ui.PosViewModelReactive>()
        val settingsViewModel = hiltViewModel<StoreSettingsViewModel>()
        val state by viewModel.uiState.collectAsState()
        val settingsState by settingsViewModel.uiState.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current
        val scope = rememberCoroutineScope()

        // Load transaction for receipt
        LaunchedEffect(transactionId) {
            viewModel.loadTransaction(transactionId)
        }

        ReceiptScreen(
            transactionNumber = state.transaction?.transactionNumber ?: "INV-XXXXX",
            total = state.total,
            cashReceived = state.transaction?.cashReceived ?: 0.0,
            cashChange = state.transaction?.cashChange ?: 0.0,
            paymentMethod = state.paymentMethod.name,
            onFinish = {
                navController.navigate(HomeRoutes.HOME) {
                    popUpTo(HomeRoutes.HOME) { inclusive = false }
                }
            },
            onPrint = {
                val tx = state.transaction ?: return@ReceiptScreen
                val items = state.transactionItems
                val settings = settingsState.settings
                val result = ReceiptPrinter.generateThermalReceiptPdf(
                    context = context,
                    settings = settings,
                    transaction = tx,
                    items = items
                )
                ReceiptPrinter.printOrSave(
                    context = context,
                    settings = settings,
                    pdfUri = result.pdfUri,
                    jobName = result.fileName
                )
            },
            onShare = {
                val tx = state.transaction ?: return@ReceiptScreen
                val items = state.transactionItems
                val settings = settingsState.settings
                val result = ReceiptPrinter.generateThermalReceiptPdf(
                    context = context,
                    settings = settings,
                    transaction = tx,
                    items = items
                )
                ReceiptPrinter.sharePdf(context, result.pdfUri)
            },
            onNewTransaction = {
                navController.navigate(PosRoutes.POS) {
                    popUpTo(HomeRoutes.HOME) { inclusive = false }
                }
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
