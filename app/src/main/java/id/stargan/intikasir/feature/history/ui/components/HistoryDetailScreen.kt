package id.stargan.intikasir.feature.history.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.feature.history.viewmodel.HistoryEvent
import id.stargan.intikasir.feature.history.viewmodel.HistoryViewModel
import id.stargan.intikasir.feature.pos.ui.components.OrderSummaryCard
import id.stargan.intikasir.ui.common.components.TransactionActions
import kotlinx.coroutines.launch
import java.text.NumberFormat
import id.stargan.intikasir.util.DateFormatUtils
import java.util.Locale


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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var printing by remember { mutableStateOf(false) }

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
                    Text("Tanggal: ${DateFormatUtils.formatEpochMillis(tx.updatedAt, "dd MMM yyyy, HH:mm")}")
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
                        onPrint = if (!uiState.isLoading && uiState.items.isNotEmpty()) {
                            {
                                if (printing) return@TransactionActions
                                printing = true
                                onPrint(tx)
                                scope.launch {
                                    kotlinx.coroutines.delay(200)
                                    printing = false
                                }
                            }
                        } else null,
                        onShare = if (!uiState.isLoading && uiState.items.isNotEmpty()) {
                            { onShare(tx) }
                        } else null,
                        onPrintQueue = if (!uiState.isLoading) {
                            {
                                if (printing) return@TransactionActions
                                onPrintQueue(tx)
                                scope.launch { snackbarHostState.showSnackbar("Tiket antrian dicetak") }
                            }
                        } else null,
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