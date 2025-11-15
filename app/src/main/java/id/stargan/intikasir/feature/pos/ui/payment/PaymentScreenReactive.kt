package id.stargan.intikasir.feature.pos.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive
import id.stargan.intikasir.feature.pos.ui.components.OrderSummaryCard
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

/**
 * Payment Screen - Reactive Version
 * Load transaction dari database dan finalize payment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreenReactive(
    transactionId: String,
    onPaymentSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PosViewModelReactive = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var customCashAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var globalDiscount by remember { mutableStateOf("") }

    // Load transaction
    LaunchedEffect(transactionId) {
        if (state.transactionId != transactionId) {
            viewModel.loadTransaction(transactionId)
        }
    }

    // Sync payment method
    LaunchedEffect(selectedPaymentMethod) {
        if (state.transaction != null && selectedPaymentMethod != state.paymentMethod) {
            viewModel.setPaymentMethod(selectedPaymentMethod)
        }
    }

    // Show messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    // Generate smart cash suggestions inline
    val cashSuggestions: List<Double> = remember(state.total) {
        if (state.total <= 0.0) listOf(1000.0, 2000.0, 5000.0) else buildList {
            val total = state.total
            val base = listOf(
                ceil(total / 1000) * 1000,
                ceil(total / 5000) * 5000,
                ceil(total / 10000) * 10000,
                ceil(total / 50000) * 50000,
                ceil(total / 100000) * 100000
            ).filter { it >= total }
            addAll(base)
            add(ceil(total / 10000) * 10000 + 10000)
            add(ceil(total / 50000) * 50000 + 50000)
        }.distinct().sorted().take(6)
    }

    // Compute per-item discount and gross subtotal for clearer breakdown
    val itemDiscountTotal = remember(state.transactionItems) { state.transactionItems.sumOf { it.discount } }
    val grossSubtotal = remember(state.transactionItems) { state.transactionItems.sumOf { it.unitPrice * it.quantity } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        scope.launch {
                            val cashReceived = when (selectedPaymentMethod) {
                                PaymentMethod.CASH -> customCashAmount.toDoubleOrNull() ?: state.total
                                else -> null
                            }

                            viewModel.finalizeTransaction(
                                cashReceived = cashReceived,
                                notes = notes.takeIf { it.isNotBlank() }
                            )

                            if (state.errorMessage == null) {
                                onPaymentSuccess(transactionId)
                            }
                        }
                    },
                    enabled = !state.isSaving && state.hasItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Memproses...")
                    } else {
                        Text("Checkout", style = MaterialTheme.typography.titleMedium)
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Order Summary (using reusable component)
            OrderSummaryCard(
                grossSubtotal = grossSubtotal,
                itemDiscount = itemDiscountTotal,
                netSubtotal = state.subtotal,
                taxRate = state.taxRate,
                taxAmount = state.tax,
                globalDiscount = state.globalDiscount,
                total = state.total
            )

            // Global Discount (compact)
            OutlinedTextField(
                value = globalDiscount,
                onValueChange = {
                    globalDiscount = it.filter { c -> c.isDigit() }
                    val amount = it.toDoubleOrNull() ?: 0.0
                    viewModel.setGlobalDiscount(amount)
                },
                label = { Text("Diskon Global") },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )

            // Payment Method - 2 columns layout
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val methods = remember { PaymentMethod.values().toList() }
            methods.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { method ->
                        val selected = selectedPaymentMethod == method
                        OutlinedButton(
                            onClick = { selectedPaymentMethod = method },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(method.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Cash received - suggestions as compact buttons that fill the field
            if (selectedPaymentMethod == PaymentMethod.CASH) {
                Text("Cash diterima", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                // suggestions rows (3 per row to keep compact)
                cashSuggestions.chunked(3).forEach { suggestionRow ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestionRow.forEach { amount ->
                            OutlinedButton(
                                onClick = {
                                    customCashAmount = amount.toInt().toString()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text(nf.format(amount).replace("Rp", "Rp "), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (suggestionRow.size < 3) {
                            repeat(3 - suggestionRow.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                OutlinedTextField(
                    value = customCashAmount,
                    onValueChange = {
                        customCashAmount = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("Cash diterima") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        val amount = customCashAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (amount < state.total) {
                                Text("Uang kurang!", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Kembali: ${nf.format(amount - state.total).replace("Rp", "Rp ")}")
                            }
                        }
                    },
                    isError = (customCashAmount.toDoubleOrNull() ?: 0.0) > 0 &&
                               (customCashAmount.toDoubleOrNull() ?: 0.0) < state.total,
                    maxLines = 1
                )
            }

            // Notes (small)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (opsional)") },
                minLines = 1,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
