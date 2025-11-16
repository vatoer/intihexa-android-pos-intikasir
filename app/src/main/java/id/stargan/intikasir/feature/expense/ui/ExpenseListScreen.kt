package id.stargan.intikasir.feature.expense.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import id.stargan.intikasir.feature.expense.ui.components.*
import id.stargan.intikasir.feature.expense.util.ExpenseExportUtil
import id.stargan.intikasir.feature.history.ui.components.DateRangePickerModal
import id.stargan.intikasir.feature.history.ui.components.formatDateRange
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    // Calculate summary
    val totalExpenses = uiState.expenses.size
    val totalAmount = uiState.dailyTotal

    // Show toast with short duration for success, default for others
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            val duration = if (message.contains("berhasil")) {
                SnackbarDuration.Short
            } else {
                SnackbarDuration.Long
            }
            snackbarHostState.showSnackbar(message, duration = duration)
            viewModel.onEvent(ExpenseEvent.DismissToast)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengeluaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ExpenseEvent.ToggleFilter) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Ringkasan CSV") },
                            onClick = {
                                scope.launch {
                                    val file = ExpenseExportUtil.exportToCSV(
                                        context,
                                        uiState.expenses,
                                        uiState.startDate,
                                        uiState.endDate
                                    )
                                    ExpenseExportUtil.shareCSV(context, file)
                                    showExportMenu = false
                                    snackbarHostState.showSnackbar("Laporan berhasil diekspor")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export Detail CSV") },
                            onClick = {
                                scope.launch {
                                    val file = ExpenseExportUtil.exportDetailedToCSV(
                                        context,
                                        uiState.expenses,
                                        uiState.startDate,
                                        uiState.endDate
                                    )
                                    ExpenseExportUtil.shareCSV(context, file)
                                    showExportMenu = false
                                    snackbarHostState.showSnackbar("Laporan detail berhasil diekspor")
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengeluaran")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter bar
            if (uiState.showFilter) {
                ExpenseFilterBar(
                    selectedRange = uiState.dateRange,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    selectedCategory = uiState.selectedCategory,
                    onRangeChange = { viewModel.onEvent(ExpenseEvent.ChangeDateRange(it)) },
                    onStartDateChange = { viewModel.onEvent(ExpenseEvent.ChangeStartDate(it)) },
                    onEndDateChange = { viewModel.onEvent(ExpenseEvent.ChangeEndDate(it)) },
                    onCategoryChange = { category ->
                        if (category == null) {
                            viewModel.onEvent(ExpenseEvent.ClearCategoryFilter)
                        } else {
                            viewModel.onEvent(ExpenseEvent.SelectCategory(category))
                        }
                    },
                    onApply = { viewModel.onEvent(ExpenseEvent.ApplyFilter) },
                    onShowCustomDatePicker = { showCustomDatePicker = true }
                )
            }

            // Summary card
            if (!uiState.isLoading && uiState.expenses.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
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
                            Column {
                                Text(
                                    text = "Total Pengeluaran",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = formatDateRange(uiState.startDate, uiState.endDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val currency = remember { java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
                                Text(
                                    text = currency.format(totalAmount).replace("Rp", "Rp "),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "$totalExpenses transaksi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Category filter chips (removed - now in filter bar)

            // Expense list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Belum ada pengeluaran",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.expenses) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onClick = { onExpenseClick(expense.id) }
                        )
                    }
                }
            }
        }
    }

    // Custom date range picker
    if (showCustomDatePicker) {
        DateRangePickerModal(
            showDialog = showCustomDatePicker,
            onDismiss = { showCustomDatePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.onEvent(ExpenseEvent.ChangeStartDate(start))
                viewModel.onEvent(ExpenseEvent.ChangeEndDate(end))
                viewModel.onEvent(ExpenseEvent.ChangeDateRange(ExpenseDateRange.CUSTOM))
                showCustomDatePicker = false
            }
        )
    }
}

@Composable
private fun ExpenseFilterBar(
    selectedRange: ExpenseDateRange,
    startDate: Long,
    endDate: Long,
    selectedCategory: ExpenseCategory?,
    onRangeChange: (ExpenseDateRange) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onCategoryChange: (ExpenseCategory?) -> Unit,
    onApply: () -> Unit,
    onShowCustomDatePicker: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date range chips
        Text("Periode", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseDateRange.values().forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = {
                        if (range == ExpenseDateRange.CUSTOM) {
                            onShowCustomDatePicker()
                        } else {
                            onRangeChange(range)
                        }
                    },
                    label = { Text(range.label) }
                )
            }
        }

        // Show date range for custom
        if (selectedRange == ExpenseDateRange.CUSTOM) {
            OutlinedButton(
                onClick = { onShowCustomDatePicker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatDateRange(startDate, endDate))
            }
        }

        // Category filter
        Text("Kategori", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategoryChange(null) },
                label = { Text("Semua") }
            )
            ExpenseCategory.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(getCategoryLabel(category)) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onApply) { Text("Terapkan") }
        }
    }
}

