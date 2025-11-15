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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive
import id.stargan.intikasir.feature.pos.ui.components.PosProductItemReactive
import java.text.NumberFormat
import java.util.Locale

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
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())

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
                    // Summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal")
                                Text(nf.format(state.subtotal).replace("Rp", "Rp "))
                            }
                            if (state.taxRate > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pajak (${(state.taxRate * 100).toInt()}%)")
                                    Text(nf.format(state.tax).replace("Rp", "Rp "))
                                }
                            }
                            if (state.globalDiscount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Diskon")
                                    Text(
                                        "-${nf.format(state.globalDiscount).replace("Rp", "Rp ")}",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    nf.format(state.total).replace("Rp", "Rp "),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

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

