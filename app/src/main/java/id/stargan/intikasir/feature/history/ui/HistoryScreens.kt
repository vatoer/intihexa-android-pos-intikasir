package id.stargan.intikasir.feature.history.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.feature.history.viewmodel.HistoryEvent
import id.stargan.intikasir.feature.history.viewmodel.HistoryViewModel
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
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 } }

    // Calculate summary
    val totalTransactions = uiState.transactions.size
    val totalRevenue = uiState.transactions.sumOf { it.total }

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
                }
            )
        }
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
                    onRangeChange = { viewModel.onEvent(HistoryEvent.ChangeRange(it)) },
                    onStartDateChange = { viewModel.onEvent(HistoryEvent.ChangeStartDate(it)) },
                    onEndDateChange = { viewModel.onEvent(HistoryEvent.ChangeEndDate(it)) },
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
    onRangeChange: (DateRange) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onApply: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Preset chips
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRange.values().forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeChange(range) },
                    label = { Text(range.label) }
                )
            }
        }
        if (selectedRange == DateRange.CUSTOM) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDate.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dari") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sampai") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onApply) { Text("Terapkan") }
        }
    }
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
    isAdmin: Boolean = false,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 } }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    LaunchedEffect(transactionId) { viewModel.onEvent(HistoryEvent.LoadDetail(transactionId)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detil Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
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
                // Totals section
                item {
                    val subtotalStr = currency.format(tx.subtotal).replace("Rp", "Rp ")
                    val taxStr = currency.format(tx.tax).replace("Rp", "Rp ")
                    val discountStr = currency.format(tx.discount).replace("Rp", "Rp ")
                    val totalStr = currency.format(tx.total).replace("Rp", "Rp ")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Subtotal: $subtotalStr")
                        if (tx.tax > 0) Text("PPN: $taxStr")
                        if (tx.discount > 0) Text("Diskon: -$discountStr")
                        Text("Total: $totalStr", fontWeight = FontWeight.Bold)
                        if (tx.cashReceived > 0) {
                            val receivedStr = currency.format(tx.cashReceived).replace("Rp", "Rp ")
                            val changeStr = currency.format(tx.cashChange).replace("Rp", "Rp ")
                            Text("Dibayar: $receivedStr")
                            Text("Kembalian: $changeStr")
                        }
                    }
                    HorizontalDivider()
                }
                // Actions - improved with Share button and Admin-only Delete
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onPrint(tx) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Print, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Cetak")
                            }
                            Button(onClick = { onShare(tx) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Bagikan")
                            }
                        }
                        // Delete button - only for Admin
                        if (isAdmin) {
                            OutlinedButton(
                                onClick = { onDelete(tx) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Hapus Transaksi (Admin)")
                            }
                        }
                    }
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
    unitPrice: Double,
    discount: Double,
    subtotal: Double,
    currency: NumberFormat
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            val unitStr = currency.format(unitPrice).replace("Rp", "Rp ")
            val subStr = currency.format(subtotal).replace("Rp", "Rp ")
            Text("$quantity x $unitStr = $subStr", style = MaterialTheme.typography.bodySmall)
            if (discount > 0) {
                val discountStr = currency.format(discount).replace("Rp", "Rp ")
                Text("Diskon item: -$discountStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
