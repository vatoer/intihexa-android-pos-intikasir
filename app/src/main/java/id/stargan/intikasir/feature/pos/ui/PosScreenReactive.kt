@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE")
package id.stargan.intikasir.feature.pos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.pos.ui.components.CartSummaryReactive
import id.stargan.intikasir.feature.pos.ui.components.PosProductItemReactive
import id.stargan.intikasir.feature.home.ui.HomeViewModel
import kotlinx.coroutines.launch

/**
 * POS Screen - Reactive Version
 * Database-driven dengan auto-save setiap perubahan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreenReactive(
    modifier: Modifier = Modifier,
    transactionId: String? = null,
    onNavigateToCart: (String) -> Unit,
    onNavigateToPayment: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PosViewModelReactive = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize transaction only when we have a user or an explicit transactionId
    LaunchedEffect(transactionId, currentUser?.id) {
        if (state.transactionId == null) {
            when {
                transactionId != null -> viewModel.loadTransaction(transactionId)
                currentUser != null -> viewModel.initializeTransaction(
                    cashierId = currentUser!!.id,
                    cashierName = currentUser!!.name
                )
                else -> { /* wait until user loaded */ }
            }
        }
    }

    // If user not ready yet, show loading to avoid FK violation
    if (state.transactionId == null && transactionId == null && currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Show messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kasir") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            if (state.hasUnsavedChanges) {
                                viewModel.saveToDatabase()
                            }
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    // Delete/Clear cart icon
                    IconButton(
                        onClick = { if (state.hasItems) showClearConfirm = true },
                        enabled = state.hasItems
                    ) {
                        Icon(Icons.Default.Delete, "Kosongkan")
                    }

                    // Cart icon with badge
                    BadgedBox(
                        badge = {
                            if (state.totalQuantity > 0) {
                                Badge { Text(state.totalQuantity.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (viewModel.saveToDatabase()) {
                                        state.transactionId?.let { onNavigateToCart(it) }
                                    } else {
                                        snackbarHostState.showSnackbar("Gagal menyimpan transaksi")
                                    }
                                }
                            },
                            enabled = state.hasItems
                        ) {
                            Icon(Icons.Default.ShoppingCart, "Keranjang")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        scope.launch {
                            if (viewModel.saveToDatabase()) {
                                state.transactionId?.let { onNavigateToPayment(it) }
                            } else {
                                snackbarHostState.showSnackbar("Gagal menyimpan transaksi")
                            }
                        }
                    },
                    enabled = state.hasItems && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Menyimpan...")
                    } else {
                        Text("Lanjut ke Pembayaran (${state.transactionItems.size} item)")
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top: Cart Summary
            CartSummaryReactive(state = state)

            // Middle: Search + Product List (scrollable)
            TextField(
                value = searchState.value,
                onValueChange = {
                    searchState.value = it
                    viewModel.onSearchChange(it.text)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Cari produk...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            HorizontalDivider()

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    // Safety: if fresh install and seeding just happened, products might still be empty momentarily.
                    val noProductsYet = state.products.isEmpty()
                    var retried by remember { mutableStateOf(false) }
                    LaunchedEffect(noProductsYet, retried) {
                        if (noProductsYet && !retried) {
                            // give a tiny grace period for seeding/flows to emit
                            kotlinx.coroutines.delay(300)
                            retried = true
                        }
                    }

                    if (noProductsYet && retried) {
                        Text(
                            text = "Menyiapkan data produk awal...",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val filtered = state.products.filter { p ->
                            val q = state.searchQuery.trim().lowercase()
                            if (q.isEmpty()) true
                            else p.name.lowercase().contains(q) || (p.barcode?.lowercase()?.contains(q) == true)
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtered, key = { it.id }) { product ->
                                val item = state.transactionItems.find { it.productId == product.id }
                                PosProductItemReactive(
                                    product = product,
                                    transactionItem = item,
                                    onAdd = { viewModel.addOrIncrement(product.id) },
                                    onChangeQty = { qty -> viewModel.setQuantity(product.id, qty) },
                                    onSetDiscount = { discount -> viewModel.setItemDiscount(product.id, discount) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear cart confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Kosongkan Keranjang?") },
            text = { Text("Semua item akan dihapus dari keranjang.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCart()
                    showClearConfirm = false
                }) { Text("Ya, Kosongkan") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Batal") }
            }
        )
    }
}
