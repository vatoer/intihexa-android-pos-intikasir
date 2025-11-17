package id.stargan.intikasir.feature.home.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.navigation.compose.navigation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
import id.stargan.intikasir.feature.pos.print.ESCPosPrinter
import id.stargan.intikasir.feature.history.ui.HistoryScreen
import id.stargan.intikasir.feature.history.ui.HistoryDetailScreen
import id.stargan.intikasir.feature.expense.ui.ExpenseListScreen
import id.stargan.intikasir.feature.expense.ui.ExpenseFormScreen
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.domain.model.UserRole
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.profile.ui.ProfileScreen
import id.stargan.intikasir.feature.users.ui.UsersManagementScreen
import id.stargan.intikasir.feature.reports.ui.ReportsScreen

/**
 * Navigation graph untuk Home feature
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    composable(HomeRoutes.HOME) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val homeVm = hiltViewModel<id.stargan.intikasir.feature.home.ui.HomeViewModel>()
        val userState = homeVm.currentUser.collectAsState()

        // Force logout if no current user (e.g., after reinstall) after a short grace
        LaunchedEffect(userState.value) {
            if (userState.value == null) {
                kotlinx.coroutines.delay(400)
                if (homeVm.currentUser.value == null) {
                    android.widget.Toast.makeText(context, "Sesi berakhir, silakan login lagi", android.widget.Toast.LENGTH_SHORT).show()
                    onLogout()
                    return@LaunchedEffect
                }
            }
        }

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
        HistoryScreen(
            onBack = { navController.navigateUp() },
            onOpenDetail = { txId -> navController.navigate("${HomeRoutes.HISTORY}/detail/$txId") }
        )
    }

    composable("${HomeRoutes.HISTORY}/detail/{transactionId}", arguments = listOf(
        navArgument("transactionId") { type = NavType.StringType }
    )) { backStackEntry ->
        val txId = backStackEntry.arguments?.getString("transactionId")!!
        val settingsViewModel = hiltViewModel<StoreSettingsViewModel>()
        val settingsState by settingsViewModel.uiState.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current
        val scope = rememberCoroutineScope()
        val posVm = hiltViewModel<id.stargan.intikasir.feature.pos.ui.PosViewModelReactive>()
        val historyVm = hiltViewModel<id.stargan.intikasir.feature.history.viewmodel.HistoryViewModel>()

        // Get current user role using GetCurrentUserUseCase
        val getCurrentUserUseCase: GetCurrentUserUseCase = hiltViewModel<HistoryRoleViewModel>().getCurrentUserUseCase
        val currentUser by getCurrentUserUseCase().collectAsState(initial = null)

        HistoryDetailScreen(
            transactionId = txId,
            onBack = { navController.navigateUp() },
            onPrint = { tx ->
                scope.launch {
                    posVm.loadTransaction(txId)
                    val items = posVm.uiState.value.transactionItems
                    val settings = settingsState.settings

                    val result = ReceiptPrinter.printReceiptOrPdf(context, settings, tx, items)

                    // Show feedback
                    result?.let { printResult ->
                        when (printResult) {
                            is ESCPosPrinter.PrintResult.Success -> {
                                Toast.makeText(context, "Struk berhasil dicetak", Toast.LENGTH_SHORT).show()
                            }
                            is ESCPosPrinter.PrintResult.Error -> {
                                Toast.makeText(context, "Gagal mencetak: ${printResult.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } ?: run {
                        // PDF generated (no ESC/POS result)
                        Toast.makeText(context, "Struk berhasil dibuat", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onShare = { tx ->
                scope.launch {
                    posVm.loadTransaction(txId)
                    val items = posVm.uiState.value.transactionItems
                    val settings = settingsState.settings
                    val result = ReceiptPrinter.generateThermalReceiptPdf(context, settings, tx, items)
                    ReceiptPrinter.sharePdf(context, result.pdfUri)
                }
            },
            onDelete = { tx ->
                // Dialog is shown by ViewModel, just navigate back after confirm
                navController.navigateUp()
            },
            onEdit = { transactionId ->
                // Navigate to POS screen with transaction ID for editing
                navController.navigate(PosRoutes.pos(transactionId)) {
                    popUpTo(HomeRoutes.HISTORY) { inclusive = false }
                }
            },
            onPrintQueue = { tx ->
                val settings = settingsState.settings
                val result = ReceiptPrinter.printQueueOrPdf(context, settings, tx)
                // Handle result if ESC/POS direct print was used
                result?.let { printResult ->
                    when (printResult) {
                        is ESCPosPrinter.PrintResult.Success -> {
                            Toast.makeText(context, "Antrian berhasil dicetak", Toast.LENGTH_SHORT).show()
                        }
                        is ESCPosPrinter.PrintResult.Error -> {
                            Toast.makeText(context, "Gagal mencetak antrian: ${printResult.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onComplete = { tx ->
                scope.launch {
                    posVm.completeTransaction(tx.id)
                }
            },
            isAdmin = currentUser?.role == UserRole.ADMIN
        )
    }

    composable(HomeRoutes.EXPENSES) {
        ExpenseListScreen(
            onBack = { navController.navigateUp() },
            onAddExpense = { navController.navigate("${HomeRoutes.EXPENSES}/add") },
            onExpenseClick = { expenseId ->
                navController.navigate("${HomeRoutes.EXPENSES}/detail/$expenseId")
            }
        )
    }

    composable("${HomeRoutes.EXPENSES}/add") {
        ExpenseFormScreen(
            onBack = { navController.navigateUp() },
            onSaveSuccess = { navController.navigateUp() }
        )
    }

    composable("${HomeRoutes.EXPENSES}/detail/{expenseId}") {
        // TODO: Implement detail/edit screen
        PlaceholderScreen(
            title = "Detail Pengeluaran",
            onBack = { navController.navigateUp() }
        )
    }

    composable(HomeRoutes.REPORTS) {
        ReportsScreen(
            onNavigateBack = { navController.navigateUp() }
        )
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
            transactionStatus = state.transaction?.status ?: TransactionStatus.PAID,
            onFinish = {
                navController.navigate(HomeRoutes.HOME) {
                    popUpTo(HomeRoutes.HOME) { inclusive = false }
                }
            },
            onPrint = {
                val tx = state.transaction ?: return@ReceiptScreen
                val items = state.transactionItems
                val settings = settingsState.settings

                // Use helper function for consistency
                val result = ReceiptPrinter.printReceiptOrPdf(context, settings, tx, items)

                // Show feedback
                result?.let { printResult ->
                    when (printResult) {
                        is ESCPosPrinter.PrintResult.Success -> {
                            Toast.makeText(context, "Struk berhasil dicetak", Toast.LENGTH_SHORT).show()
                        }
                        is ESCPosPrinter.PrintResult.Error -> {
                            Toast.makeText(context, "Gagal mencetak: ${printResult.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } ?: run {
                    Toast.makeText(context, "Struk berhasil dibuat", Toast.LENGTH_SHORT).show()
                }
            },
            onPrintQueue = {
                val tx = state.transaction ?: return@ReceiptScreen
                val settings = settingsState.settings
                // Use ESC/POS if available; otherwise fallback to PDF
                val result = ReceiptPrinter.printQueueOrPdf(
                    context = context,
                    settings = settings,
                    transaction = tx
                )
                // Handle result if ESC/POS direct print was used
                result?.let { printResult ->
                    when (printResult) {
                        is ESCPosPrinter.PrintResult.Success -> {
                            Toast.makeText(context, "Antrian berhasil dicetak", Toast.LENGTH_SHORT).show()
                        }
                        is ESCPosPrinter.PrintResult.Error -> {
                            Toast.makeText(context, "Gagal mencetak antrian: ${printResult.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
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
            onComplete = {
                scope.launch {
                    viewModel.completeTransaction(transactionId)
                }
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

    composable(HomeRoutes.PROFILE) {
        ProfileScreen(onNavigateBack = { navController.navigateUp() })
    }
    composable(HomeRoutes.USERS) {
        UsersManagementScreen(onNavigateBack = { navController.navigateUp() })
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

// Simple ViewModel to expose GetCurrentUserUseCase for navigation
@HiltViewModel
class HistoryRoleViewModel @Inject constructor(
    val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel()
