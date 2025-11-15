package id.stargan.intikasir.feature.pos.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive
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
    var selectedCashAmount by remember { mutableStateOf<Double?>(null) }
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

    // Generate smart cash suggestions
    val cashSuggestions = remember(state.total) {
        generateSmartCashSuggestions(state.total)
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
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary
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
                    Text(
                        "Ringkasan Pesanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()

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
                            Text("PPN (${(state.taxRate * 100).toInt()}%)")
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

            // Global Discount
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
                modifier = Modifier.fillMaxWidth()
            )

            // Payment Method Selection
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            PaymentMethod.values().forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method }
                        )
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == method,
                        onClick = { selectedPaymentMethod = method }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(method.name, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
                }
            }

            // Cash amount selection (only for CASH method)
            if (selectedPaymentMethod == PaymentMethod.CASH) {
                Text("Nominal Uang Tunai", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                cashSuggestions.forEach { amount ->
                    val change = amount - state.total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedCashAmount == amount,
                                onClick = {
                                    selectedCashAmount = amount
                                    customCashAmount = ""
                                }
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedCashAmount == amount,
                            onClick = {
                                selectedCashAmount = amount
                                customCashAmount = ""
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nf.format(amount).replace("Rp", "Rp "))
                            Text(
                                "Kembali: ${nf.format(change).replace("Rp", "Rp ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                               (customCashAmount.toDoubleOrNull() ?: 0.0) < state.total
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan (opsional)") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

