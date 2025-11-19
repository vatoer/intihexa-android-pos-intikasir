package id.stargan.intikasir.feature.history.ui.screens

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
import id.stargan.intikasir.feature.history.viewmodel.HistoryEvent
import id.stargan.intikasir.feature.history.viewmodel.HistoryViewModel
import id.stargan.intikasir.feature.history.ui.components.HistoryFilterBar
import id.stargan.intikasir.feature.history.ui.components.TransactionRow
import id.stargan.intikasir.feature.history.util.ExportUtil
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * History list screen showing all transactions with filtering and export capabilities.
 */
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

