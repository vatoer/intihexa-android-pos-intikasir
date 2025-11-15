package id.stargan.intikasir.feature.pos.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive
import id.stargan.intikasir.feature.pos.ui.components.OrderSummaryCard
import id.stargan.intikasir.feature.pos.ui.components.PosProductItemReactive

/**
 * Cart Screen - Reactive Version
 * Load data dari database berdasarkan transactionId
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreenReactive(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PosViewModelReactive = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Calculate item-level discount and gross subtotal
    val itemDiscountTotal = remember(state.transactionItems) {
        state.transactionItems.sumOf { it.discount }
    }
    val grossSubtotal = remember(state.transactionItems) {
        state.transactionItems.sumOf { it.unitPrice * it.quantity }
    }

    // Load transaction
    LaunchedEffect(transactionId) {
        if (state.transactionId != transactionId) {
            viewModel.loadTransaction(transactionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Belanja") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    // Summary using reusable component
                    OrderSummaryCard(
                        grossSubtotal = grossSubtotal,
                        itemDiscount = itemDiscountTotal,
                        netSubtotal = state.subtotal,
                        taxRate = state.taxRate,
                        taxAmount = state.tax,
                        globalDiscount = state.globalDiscount,
                        total = state.total
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment button
                    Button(
                        onClick = { onNavigateToPayment(transactionId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.hasItems && !state.isSaving
                    ) {
                        Text("Lanjut ke Pembayaran", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!state.hasItems) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Keranjang Kosong",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.transactionItems, key = { it.id }) { item ->
                    val product = state.products.find { it.id == item.productId }
                    if (product != null) {
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

