package id.stargan.intikasir.feature.expense.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.expense.ui.components.getCategoryLabel
import id.stargan.intikasir.ui.common.CurrencyVisualTransformation
import id.stargan.intikasir.ui.common.parseRupiah
import id.stargan.intikasir.util.DateFormatUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFormScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var category by remember { mutableStateOf(ExpenseCategory.MISC) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val dateFormat = remember { id.stargan.intikasir.util.DateFormatUtils }

    // Show toast for errors only, navigate back immediately on success
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            if (message.contains("berhasil")) {
                // Success: navigate back immediately without showing toast here
                viewModel.onEvent(ExpenseEvent.DismissToast)
                onSaveSuccess()
            } else {
                // Error: show toast with longer duration
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long
                )
                viewModel.onEvent(ExpenseEvent.DismissToast)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Pengeluaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Category selector
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = getCategoryLabel(category),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    ExpenseCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(getCategoryLabel(cat)) },
                            onClick = {
                                category = cat
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Date selector
            OutlinedTextField(
                value = DateFormatUtils.formatEpochMillis(selectedDate, "dd MMMM yyyy"),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Jumlah") },
                leadingIcon = { Text("Rp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Keterangan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Payment method selector
            ExposedDropdownMenuBox(
                expanded = showPaymentMenu,
                onExpandedChange = { showPaymentMenu = it }
            ) {
                OutlinedTextField(
                    value = when (paymentMethod) {
                        PaymentMethod.CASH -> "Tunai"
                        PaymentMethod.QRIS -> "QRIS"
                        PaymentMethod.CARD -> "Kartu"
                        PaymentMethod.TRANSFER -> "Transfer"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Metode Pembayaran") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = showPaymentMenu,
                    onDismissRequest = { showPaymentMenu = false }
                ) {
                    listOf(
                        PaymentMethod.CASH to "Tunai",
                        PaymentMethod.TRANSFER to "Transfer",
                        PaymentMethod.QRIS to "QRIS",
                        PaymentMethod.CARD to "Kartu"
                    ).forEach { (method, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                paymentMethod = method
                                showPaymentMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    val amountValue = parseRupiah(amount)
                    if (amountValue <= 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Jumlah harus lebih dari 0")
                        }
                        return@Button
                    }
                    if (description.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Keterangan tidak boleh kosong")
                        }
                        return@Button
                    }

                    val expense = ExpenseEntity(
                        date = selectedDate, // Use selected date instead of current time
                        category = category,
                        amount = amountValue,
                        description = description.trim(),
                        paymentMethod = paymentMethod,
                        createdBy = "", // Will be set by ViewModel
                        createdByName = "" // Will be set by ViewModel
                    )
                    viewModel.onEvent(ExpenseEvent.CreateExpense(expense))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && description.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Simpan Pengeluaran")
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = millis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Pilih Tanggal",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}
