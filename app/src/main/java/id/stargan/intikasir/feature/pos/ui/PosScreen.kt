package id.stargan.intikasir.feature.pos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.pos.ui.components.CartSummary
import id.stargan.intikasir.feature.pos.ui.components.PayButton
import id.stargan.intikasir.feature.pos.ui.components.PosProductItem
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * POS Screen (Portrait) with three vertical zones:
 * Top: Cart Summary (non-scroll)
 * Middle: Scrollable product list
 * Bottom: Floating Pay Button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onPay: (Double) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PosViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val searchState = remember { mutableStateOf(TextFieldValue("")) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var cashText by remember(showPaymentDialog) { mutableStateOf(state.total.toInt().toString()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(state.paymentError) {
        state.paymentError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPaymentError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveDraftTransaction(cashierId = "cashier-1", cashierName = "Kasir")
                            }
                        },
                        enabled = state.cartItems.isNotEmpty() && !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        else Text("Simpan")
                    }

                    Button(
                        onClick = { showPaymentDialog = true },
                        enabled = state.cartItems.isNotEmpty() && !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) { Text("Bayar") }
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
            CartSummary(state = state, onClear = { viewModel.clearCart() })

            // Global Discount & Payment Method Row (Remove PPN input - now from settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.discountGlobal.takeIf { it > 0 }?.toInt()?.toString() ?: "",
                    onValueChange = { txt ->
                        val v = txt.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                        viewModel.setGlobalDiscount(v)
                    },
                    label = { Text("Diskon") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Payment method selector
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = state.paymentMethod.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Metode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).weight(1f)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(text = { Text(method.name) }, onClick = {
                                viewModel.setPaymentMethod(method)
                                expanded = false
                            })
                        }
                    }
                }
            }

            // Show tax info if enabled (read-only)
            if (state.taxRate > 0) {
                Text(
                    text = "PPN: ${(state.taxRate * 100).toInt()}% (diatur di Pengaturan Toko)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

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
                    val filtered = state.products.filter { p ->
                        val q = state.searchQuery.trim().lowercase()
                        if (q.isEmpty()) true else p.name.lowercase().contains(q) || (p.barcode?.lowercase()?.contains(q) == true)
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { product ->
                            val item = state.cart[product.id]
                            PosProductItem(
                                product = product,
                                cartItem = item,
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

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val received = if (state.paymentMethod == PaymentMethod.CASH) (cashText.toDoubleOrNull() ?: 0.0) else null
                            viewModel.finalizeTransaction(cashierId = "cashier-1", cashierName = "Kasir", cashReceived = received)
                            if (state.paymentError == null) {
                                showPaymentDialog = false
                                onPay(state.total)
                            }
                        }
                    },
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    else Text("Konfirmasi")
                }
            },
            dismissButton = { TextButton(onClick = { showPaymentDialog = false }) { Text("Batal") } },
            title = { Text("Konfirmasi Pembayaran") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total: Rp ${state.total.toInt()}", style = MaterialTheme.typography.titleMedium)
                    if (state.paymentMethod == PaymentMethod.CASH) {
                        OutlinedTextField(
                            value = cashText,
                            onValueChange = { cashText = it.filter { c -> c.isDigit() } },
                            label = { Text("Tunai Diterima") },
                            prefix = { Text("Rp ") },
                            singleLine = true,
                            isError = (cashText.toDoubleOrNull() ?: 0.0) < state.total
                        )
                        val received = cashText.toDoubleOrNull() ?: 0.0
                        if (received < state.total) {
                            Text("Uang kurang!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        } else {
                            val change = received - state.total
                            Text("Kembali: Rp ${change.toInt()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (state.discountGlobal > 0) Text("Diskon Global: Rp ${state.discountGlobal.toInt()}")
                    if (state.taxRate > 0) Text("PPN: ${(state.taxRate * 100).toInt()}%")
                    Text("Metode: ${state.paymentMethod.name}", style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}
