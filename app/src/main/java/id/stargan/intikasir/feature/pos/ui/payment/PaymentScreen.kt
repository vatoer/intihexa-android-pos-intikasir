package id.stargan.intikasir.feature.pos.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.pos.ui.PosViewModel
import id.stargan.intikasir.feature.home.ui.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Payment/Checkout Screen
 * Menampilkan detail pembayaran, metode pembayaran, dan pilihan uang tunai
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onPaymentSuccess: (String) -> Unit, // transactionId
    onSaveDraft: (String) -> Unit, // transactionId
    modifier: Modifier = Modifier,
    viewModel: PosViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var discountGlobal by remember { mutableStateOf(state.discountGlobal.toInt().toString()) }
    var selectedPaymentMethod by remember { mutableStateOf(state.paymentMethod) }
    var customCashAmount by remember { mutableStateOf("") }
    var selectedCashAmount by remember { mutableStateOf<Double?>(null) }
    var notes by remember { mutableStateOf("") }

    // Update discount ke viewmodel
    LaunchedEffect(discountGlobal) {
        val amount = discountGlobal.toDoubleOrNull() ?: 0.0
        viewModel.setGlobalDiscount(amount)
    }

    // Update payment method
    LaunchedEffect(selectedPaymentMethod) {
        viewModel.setPaymentMethod(selectedPaymentMethod)
    }

    // Show errors
    LaunchedEffect(state.paymentError) {
        state.paymentError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPaymentError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveDraftTransaction(
                                    cashierId = "cashier-1",
                                    cashierName = "Kasir",
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                                state.lastSavedTransactionId?.let { onSaveDraft(it) }
                            }
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        else Text("Simpan")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val cashReceived = when (selectedPaymentMethod) {
                                    PaymentMethod.CASH -> {
                                        selectedCashAmount ?: customCashAmount.toDoubleOrNull() ?: state.total
                                    }
                                    else -> null
                                }

                                viewModel.finalizeTransaction(
                                    cashierId = currentUser?.id ?: "",
                                    cashierName = currentUser?.name ?: "Kasir",
                                    cashReceived = cashReceived,
                                    notes = notes.takeIf { it.isNotBlank() }
                                )

                                if (state.paymentError == null) {
                                    state.lastSavedTransactionId?.let { onPaymentSuccess(it) }
                                }
                            }
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        else Text("Checkout")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ringkasan Pesanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text("Rp ${state.subtotal.toInt()}")
                    }

                    if (state.taxRate > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PPN (${(state.taxRate * 100).toInt()}%)")
                            Text("Rp ${state.tax.toInt()}")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon")
                        Text("- Rp ${state.discountGlobal.toInt()}")
                    }

                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Rp ${state.total.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Discount Input
            OutlinedTextField(
                value = discountGlobal,
                onValueChange = { discountGlobal = it.filter { c -> c.isDigit() } },
                label = { Text("Diskon Global") },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Payment Method Selection
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            PaymentMethod.values().forEach { method ->
                FilterChip(
                    selected = selectedPaymentMethod == method,
                    onClick = { selectedPaymentMethod = method },
                    label = { Text(method.name) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Cash Amount Selection (only for CASH)
            if (selectedPaymentMethod == PaymentMethod.CASH) {
                Text("Nominal Uang Tunai", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                val suggestedAmounts = generateSmartCashSuggestions(state.total)

                suggestedAmounts.forEach { amount ->
                    FilterChip(
                        selected = selectedCashAmount == amount,
                        onClick = {
                            selectedCashAmount = amount
                            customCashAmount = ""
                        },
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rp ${amount.toInt()}")
                                if (amount >= state.total) {
                                    val change = amount - state.total
                                    Text("Kembali: Rp ${change.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = customCashAmount,
                    onValueChange = {
                        customCashAmount = it.filter { c -> c.isDigit() }
                        selectedCashAmount = null
                    },
                    label = { Text("Nominal Lainnya") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    isError = customCashAmount.toDoubleOrNull()?.let { it < state.total } == true,
                    supportingText = {
                        customCashAmount.toDoubleOrNull()?.let { amount ->
                            if (amount < state.total) {
                                Text("Uang kurang!", color = MaterialTheme.colorScheme.error)
                            } else {
                                val change = amount - state.total
                                Text("Kembali: Rp ${change.toInt()}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (opsional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Generate smart cash amount suggestions
 * Contoh: total 87.000 → suggest [90.000, 100.000, 150.000, 200.000]
 */
fun generateSmartCashSuggestions(total: Double): List<Double> {
    val suggestions = mutableListOf<Double>()

    // Round up to nearest 5k, 10k, 50k, 100k
    val roundedToNearest5k = ceil(total / 5000) * 5000
    val roundedToNearest10k = ceil(total / 10000) * 10000
    val roundedToNearest50k = ceil(total / 50000) * 50000
    val roundedToNearest100k = ceil(total / 100000) * 100000

    // Add exact amount if not already rounded
    if (roundedToNearest5k > total) suggestions.add(roundedToNearest5k)
    if (roundedToNearest10k > total && roundedToNearest10k != roundedToNearest5k) {
        suggestions.add(roundedToNearest10k)
    }
    if (roundedToNearest50k > total && roundedToNearest50k != roundedToNearest10k) {
        suggestions.add(roundedToNearest50k)
    }
    if (roundedToNearest100k > total && roundedToNearest100k != roundedToNearest50k) {
        suggestions.add(roundedToNearest100k)
    }

    // Always suggest next 100k if total > 50k
    if (total > 50000) {
        val next100k = ceil(total / 100000) * 100000
        if (next100k > total && !suggestions.contains(next100k)) {
            suggestions.add(next100k)
        }
    }

    return suggestions.distinct().sorted().take(4)
}

