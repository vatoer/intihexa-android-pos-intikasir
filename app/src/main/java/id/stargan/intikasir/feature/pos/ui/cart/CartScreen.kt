package id.stargan.intikasir.feature.pos.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.feature.pos.ui.PosViewModel
import id.stargan.intikasir.feature.pos.ui.components.PosProductItem
import id.stargan.intikasir.feature.home.ui.HomeViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Cart Screen - Dedicated screen untuk review keranjang
 * Dengan tombol Simpan | Bayar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPayment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PosViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    val scope = rememberCoroutineScope()

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
                    // Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal")
                                Text(nf.format(state.subtotal).replace("Rp", "Rp "))
                            }
                            if (state.taxRate > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pajak (${(state.taxRate * 100).toInt()}%)")
                                    Text(nf.format(state.tax).replace("Rp", "Rp "))
                                }
                            }
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

                    // Action buttons
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.saveDraftTransaction(
                                    cashierId = currentUser?.id ?: "",
                                    cashierName = currentUser?.name ?: "Kasir",
                                    clearCart = false
                                )
                                onNavigateToPayment()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.cartItems.isNotEmpty()
                    ) {
                        Text("Lanjut ke Pembayaran", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        if (state.cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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
                items(state.cartItems, key = { it.productId }) { cartItem ->
                    val product = state.products.find { it.id == cartItem.productId }
                    if (product != null) {
                        PosProductItem(
                            product = product,
                            cartItem = cartItem,
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
