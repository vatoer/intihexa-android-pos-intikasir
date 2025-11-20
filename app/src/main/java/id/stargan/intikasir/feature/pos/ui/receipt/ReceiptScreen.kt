package id.stargan.intikasir.feature.pos.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.pos.ui.receipt.components.ReceiptSuccessHeader
import id.stargan.intikasir.ui.common.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import android.util.Log

/**
 * Receipt Screen - Struk pembayaran setelah checkout berhasil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transactionNumber: String,
    total: Double,
    cashReceived: Double,
    cashChange: Double,
    paymentMethod: String,
    globalDiscount: Double = 0.0,
    transactionStatus: TransactionStatus = TransactionStatus.PAID,
    onFinish: () -> Unit,
    onPrint: (onResult: (Boolean, String) -> Unit) -> Unit,
    onPrintQueue: (onResult: (Boolean, String) -> Unit) -> Unit,
    onShare: () -> Unit,
    onComplete: () -> Unit,
    onNewTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    val notificationState = rememberSubtleNotificationState()
    val scope = rememberCoroutineScope()
    var isPrinting by remember { mutableStateOf(false) }
    var isPrintingQueue by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(transactionStatus) }
    val isCompleted = currentStatus == TransactionStatus.COMPLETED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran Berhasil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
            // Compact Success Header - icon left, info right
            ReceiptSuccessHeader(
                transactionNumber = transactionNumber,
                transactionStatus = currentStatus,
                modifier = Modifier.padding(16.dp)
            )

            // Receipt details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Detail Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Metode Pembayaran")
                        Text(paymentMethod, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Belanja")
                        Text(nf.format(total).replace("Rp", "Rp "), fontWeight = FontWeight.Bold)
                    }

                    if (globalDiscount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon Global")
                            Text(
                                "- ${nf.format(globalDiscount).replace("Rp", "Rp ")}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (paymentMethod == "CASH" && cashReceived > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tunai Diterima")
                            Text(nf.format(cashReceived).replace("Rp", "Rp "))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kembalian")
                            Text(
                                nf.format(cashChange).replace("Rp", "Rp "),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons - custom implementation with printing states
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Complete button - disabled if already completed
                Button(
                    onClick = {
                        if (!isCompleted) {
                            onComplete()
                            // Update local status immediately for UI feedback
                            currentStatus = TransactionStatus.COMPLETED
                            scope.launch {
                                notificationState.show(
                                    message = "Transaksi telah diselesaikan",
                                    icon = Icons.Default.CheckCircle,
                                    type = NotificationType.Success
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPrinting && !isPrintingQueue && !isCompleted
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Done,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isCompleted) "Transaksi Selesai" else "Selesai")
                }

                if (isCompleted) {
                    Text(
                        text = "Transaksi ini sudah diselesaikan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // LaunchedEffect for print - triggered when isPrinting becomes true
                LaunchedEffect(isPrinting) {
                    if (isPrinting) {
                        Log.d("ReceiptScreen", "Print process started")
                        onPrint { success, message ->
                            scope.launch {
                                notificationState.show(
                                    message = message,
                                    icon = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    type = if (success) NotificationType.Success else NotificationType.Error,
                                    duration = if (success) 2000L else 3000L
                                )
                                // delay(100) // Small delay before re-enabling
                                isPrinting = false
                            }
                        }
                    }
                }

                // Row: Cetak & Bagikan
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (!isPrinting && !isPrintingQueue) {
                                isPrinting = true // Instant UI feedback
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isPrinting && !isPrintingQueue
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (isPrinting) "Mencetak..." else "Cetak")
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        enabled = !isPrinting && !isPrintingQueue
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Bagikan")
                    }
                }

                // LaunchedEffect for print queue - triggered when isPrintingQueue becomes true
                LaunchedEffect(isPrintingQueue) {
                    if (isPrintingQueue) {
                        Log.d("ReceiptScreen", "Print queue process started")
                        onPrintQueue { success, message ->
                            scope.launch {
                                notificationState.show(
                                    message = message,
                                    icon = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    type = if (success) NotificationType.Success else NotificationType.Error,
                                    duration = if (success) 2000L else 3000L
                                )
//                                delay(100) // Small delay before re-enabling
                                isPrintingQueue = false
                            }
                        }
                    }
                }

                // Antrian button
                OutlinedButton(
                    onClick = {
                        if (!isPrinting && !isPrintingQueue) {
                            isPrintingQueue = true // Instant UI feedback
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPrinting && !isPrintingQueue
                ) {
                    if (isPrintingQueue) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPrintingQueue) "Mencetak Antrian..." else "Cetak Antrian")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // New transaction button - full width
                Button(
                    onClick = onNewTransaction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Buat Transaksi Baru")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Back to home button - text button
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kembali ke Menu Utama")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            }

            // Subtle notification at top - non-intrusive feedback
            SubtleNotificationHost(state = notificationState)
        }
    }
}

