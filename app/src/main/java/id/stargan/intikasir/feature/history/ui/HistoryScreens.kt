package id.stargan.intikasir.feature.history.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.history.viewmodel.HistoryEvent
import id.stargan.intikasir.feature.history.viewmodel.HistoryViewModel
import id.stargan.intikasir.feature.history.ui.components.DateRangePickerModal
import id.stargan.intikasir.feature.history.ui.components.formatDateRange
import id.stargan.intikasir.feature.history.util.ExportUtil
import id.stargan.intikasir.ui.common.components.TransactionActions
import id.stargan.intikasir.feature.pos.ui.components.OrderSummaryCard
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 } }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate summary
    val totalTransactions = uiState.transactions.size
    val totalRevenue = uiState.transactions.sumOf { it.total }

    var showExportMenu by remember { mutableStateOf(false) }

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(HistoryEvent.DismissToast)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(HistoryEvent.ToggleFilter) }) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
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
                                    // Load items for all transactions
                                    val transactionIds = uiState.transactions.map { it.id }
                                    val itemsMap = viewModel.loadAllTransactionItems(transactionIds)

                                    val file = ExportUtil.exportToCSV(context, uiState.transactions, itemsMap)
                                    ExportUtil.shareCSV(context, file)
                                    showExportMenu = false
                                    snackbarHostState.showSnackbar("Laporan berhasil diekspor")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export Detail CSV") },
                            onClick = {
                                scope.launch {
                                    // Load items for all transactions
                                    val transactionIds = uiState.transactions.map { it.id }
                                    val itemsMap = viewModel.loadAllTransactionItems(transactionIds)

                                    val file = ExportUtil.exportDetailedToCSV(context, uiState.transactions, itemsMap)
                                    ExportUtil.shareCSV(context, file)
                                    showExportMenu = false
                                    snackbarHostState.showSnackbar("Laporan detail berhasil diekspor")
                                }
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.showFilter) {
                HistoryFilterBar(
                    selectedRange = uiState.range,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    selectedStatus = uiState.selectedStatus,
                    onRangeChange = { viewModel.onEvent(HistoryEvent.ChangeRange(it)) },
                    onStartDateChange = { viewModel.onEvent(HistoryEvent.ChangeStartDate(it)) },
                    onEndDateChange = { viewModel.onEvent(HistoryEvent.ChangeEndDate(it)) },
                    onStatusChange = { viewModel.onEvent(HistoryEvent.ChangeStatus(it)) },
                    onApply = { viewModel.onEvent(HistoryEvent.ApplyFilter) }
                )
            }

            // Summary Card
            if (!uiState.isLoading && uiState.transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$totalTransactions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Pendapatan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currency.format(totalRevenue).replace("Rp", "Rp "),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.transactions) { tx ->
                        TransactionRow(
                            tx = tx,
                            currency = currency,
                            dateFormatter = dateFormatter,
                            onClick = { onOpenDetail(tx.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterBar(
    selectedRange: DateRange,
    startDate: Long,
    endDate: Long,
    selectedStatus: TransactionStatus?,
    onRangeChange: (DateRange) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onStatusChange: (TransactionStatus?) -> Unit,
    onApply: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Date range chips
        Text("Periode", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRange.entries.forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeChange(range) },
                    label = { Text(range.label) }
                )
            }
        }

        // Show date range for custom
        if (selectedRange == DateRange.CUSTOM) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatDateRange(startDate, endDate))
            }
        }

        // Status filter
        Text("Status", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusChange(null) },
                label = { Text("Semua") }
            )
            TransactionStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusChange(status) },
                    label = { Text(status.name) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onApply) { Text("Terapkan") }
        }
    }

    DateRangePickerModal(
        showDialog = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateRangeSelected = { start, end ->
            onStartDateChange(start)
            onEndDateChange(end)
        }
    )
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    currency: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(tx.transactionNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(dateFormatter.format(Date(tx.updatedAt)), style = MaterialTheme.typography.bodySmall)
                Text("Kasir: ${tx.cashierName}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(currency.format(tx.total).replace("Rp", "Rp "), style = MaterialTheme.typography.titleMedium)
                Text(tx.status.name, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    transactionId: String,
    onBack: () -> Unit,
    onPrint: (TransactionEntity) -> Unit,
    onShare: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (String) -> Unit,
    onPrintQueue: (TransactionEntity) -> Unit,
    onComplete: (TransactionEntity) -> Unit,
    isAdmin: Boolean = false,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsState()
    val listUiState by viewModel.uiState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 } }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactionId) { viewModel.onEvent(HistoryEvent.LoadDetail(transactionId)) }

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(HistoryEvent.DismissToast)
        }
    }

    // Delete confirmation dialog
    if (listUiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HistoryEvent.DismissDeleteConfirmation) },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Transaksi yang dihapus tidak dapat dikembalikan. Apakah Anda yakin ingin menghapus transaksi ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(HistoryEvent.ConfirmDelete)
                        onDelete(uiState.transaction!!)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(HistoryEvent.DismissDeleteConfirmation) }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detil Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else uiState.transaction?.let { tx ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(tx.transactionNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tanggal: ${dateFormatter.format(Date(tx.updatedAt))}")
                    Text("Kasir: ${tx.cashierName}")
                    Text("Status: ${tx.status.name}")
                    Text("Metode Bayar: ${tx.paymentMethod.name}")
                    HorizontalDivider()
                }
                // Items header
                if (uiState.items.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Item Dibeli", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${uiState.items.size} item • ${uiState.items.sumOf { it.quantity }} pcs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(uiState.items) { item ->
                        ItemDetailRow(
                            name = item.productName,
                            quantity = item.quantity,
                            productPrice = item.productPrice,
                            unitPrice = item.unitPrice,
                            discount = item.discount,
                            subtotal = item.subtotal,
                            currency = currency
                        )
                    }
                    item { HorizontalDivider() }
                } else {
                    item { Text("Tidak ada item", style = MaterialTheme.typography.bodyMedium) }
                }

                // Order Summary using OrderSummaryCard component
                item {
                    // Calculate values
                    val grossSubtotal = uiState.items.sumOf { it.unitPrice * it.quantity }
                    val itemDiscount = uiState.items.sumOf { it.discount }
                    val netSubtotal = tx.subtotal
                    val taxRate = if (netSubtotal > 0 && tx.tax > 0) tx.tax / netSubtotal else 0.0
                    val globalDiscount = tx.discount

                    OrderSummaryCard(
                        grossSubtotal = grossSubtotal,
                        itemDiscount = itemDiscount,
                        netSubtotal = netSubtotal,
                        taxRate = taxRate,
                        taxAmount = tx.tax,
                        globalDiscount = globalDiscount,
                        total = tx.total
                    )

                    // Cash payment details (if applicable)
                    if (tx.cashReceived > 0) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Detail Pembayaran Tunai",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                HorizontalDivider()

                                val receivedStr = currency.format(tx.cashReceived).replace("Rp", "Rp ")
                                val changeStr = currency.format(tx.cashChange).replace("Rp", "Rp ")

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tunai Diterima", style = MaterialTheme.typography.bodySmall)
                                    Text(receivedStr, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Kembalian",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        changeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                }
                // Actions - using reusable TransactionActions component
                item {
                    TransactionActions(
                        status = tx.status,
                        onEdit = { onEdit(tx.id) },
                        onPrint = { onPrint(tx) },
                        onShare = { onShare(tx) },
                        onPrintQueue = {
                            onPrintQueue(tx)
                            scope.launch { snackbarHostState.showSnackbar("Tiket antrian dicetak") }
                        },
                        onComplete = {
                            onComplete(tx)
                            scope.launch { snackbarHostState.showSnackbar("Transaksi ditandai selesai") }
                        },
                        isAdmin = isAdmin,
                        onDeleteAdmin = { viewModel.onEvent(HistoryEvent.ShowDeleteConfirmation(tx.id)) }
                    )
                }
                // Bottom spacing
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ItemDetailRow(
    name: String,
    quantity: Int,
    productPrice: Double,
    unitPrice: Double,
    discount: Double,
    subtotal: Double,
    currency: NumberFormat
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            if (discount > 0) {
                // Show original price
                val origPriceStr = currency.format(productPrice).replace("Rp", "Rp ")
                Text(
                    "@$origPriceStr/pcs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Calculate discounted price per unit
                val discountPerUnit = discount / quantity
                val discountedPricePerUnit = productPrice - discountPerUnit
                val discountedStr = currency.format(discountedPricePerUnit).replace("Rp", "Rp ")
                val subStr = currency.format(subtotal).replace("Rp", "Rp ")

                // Show quantity x discounted price = subtotal
                Text(
                    "$quantity x $discountedStr = $subStr",
                    style = MaterialTheme.typography.bodySmall
                )

                // Show total discount
                val discountStr = currency.format(discount).replace("Rp", "Rp ")
                val discountPerUnitStr = currency.format(discountPerUnit).replace("Rp", "Rp ")
                Text(
                    "Diskon: $discountPerUnitStr/pcs (Total: -$discountStr)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // No discount - simple format
                val unitStr = currency.format(unitPrice).replace("Rp", "Rp ")
                val subStr = currency.format(subtotal).replace("Rp", "Rp ")
                Text("$quantity x $unitStr = $subStr", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

enum class DateRange(val label: String) {
    TODAY("Hari ini"),
    YESTERDAY("Kemarin"),
    LAST_7_DAYS("7 hari terakhir"),
    THIS_MONTH("Bulan ini"),
    LAST_MONTH("Bulan lalu"),
    CUSTOM("Custom")
}
