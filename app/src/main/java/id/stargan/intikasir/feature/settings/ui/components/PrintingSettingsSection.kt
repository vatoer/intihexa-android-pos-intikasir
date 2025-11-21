package id.stargan.intikasir.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider
import android.content.Context
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.pos.print.ReceiptPrinter

@Composable
fun PrintingSettingsSection(
    settings: StoreSettings,
    onSave: (StoreSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var paperWidth by remember(settings) { mutableStateOf(settings.paperWidthMm) }
    var charPerLine by remember(settings) { mutableStateOf(settings.paperCharPerLine) }
    val format = "THERMAL" // lock to thermal only
    var autoCut by remember(settings) { mutableStateOf(settings.autoCut) }
    var printLogo by remember(settings) { mutableStateOf(settings.printLogo) }
    var useEscPosDirect by remember(settings) { mutableStateOf(settings.useEscPosDirect) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pengaturan Cetak", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()

            // Format info (locked to Thermal)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Format Cetak")
                AssistChip(onClick = {}, label = { Text("Thermal (aktif)") }, leadingIcon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                }, enabled = false)
            }

            // Paper width selector
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Lebar Kertas (mm)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paperWidth == 58,
                        onClick = { paperWidth = 58; charPerLine = 32 },
                        label = { Text("58") },
                        leadingIcon = if (paperWidth == 58) { { Icon(Icons.Default.Check, contentDescription = null) } } else null
                    )
                    FilterChip(
                        selected = paperWidth == 80,
                        onClick = { paperWidth = 80; charPerLine = 48 },
                        label = { Text("80") },
                        leadingIcon = if (paperWidth == 80) { { Icon(Icons.Default.Check, contentDescription = null) } } else null
                    )
                }
            }
            // Hint chars per line
            Text(
                text = if (paperWidth == 58) "58 mm ≈ 32 karakter per baris" else "80 mm ≈ 48 karakter per baris",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Options
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Auto Cut (jika didukung)")
                Switch(checked = autoCut, onCheckedChange = { autoCut = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tampilkan Logo di Struk")
                Switch(checked = printLogo, onCheckedChange = { printLogo = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Cetak langsung ESC/POS (Bluetooth)")
                Switch(
                    checked = useEscPosDirect,
                    onCheckedChange = { useEscPosDirect = it }
                )
            }

            // Mini preview (generate thermal pdf and show hint)
            Text("Preview Thermal (contoh)", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Preview PDF akan dibuat saat Cetak/Bagikan. Untuk melihat contoh, selesaikan transaksi lalu buka struk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val context = androidx.compose.ui.platform.LocalContext.current
            OutlinedButton(
                onClick = {
                    try {
                        val effective = settings
                        val txId = UUID.randomUUID().toString()
                        val items = listOf(
                            TransactionItemEntity(
                                transactionId = txId,
                                productId = "sku-001",
                                productName = "Produk Contoh A",
                                productPrice = 10000.0,
                                quantity = 2,
                                unitPrice = 10000.0,
                                subtotal = 20000.0
                            ),
                            TransactionItemEntity(
                                transactionId = txId,
                                productId = "sku-002",
                                productName = "Produk Contoh B",
                                productPrice = 5000.0,
                                quantity = 1,
                                unitPrice = 5000.0,
                                subtotal = 5000.0
                            )
                        )
                        val subtotal = items.sumOf { it.subtotal }
                        val tax = if (effective.taxEnabled) subtotal * (effective.taxPercentage / 100.0) else 0.0
                        val total = subtotal + tax
                        val tx = TransactionEntity(
                            transactionNumber = "PREVIEW-${System.currentTimeMillis()}",
                            cashierId = "preview",
                            cashierName = "Preview",
                            paymentMethod = PaymentMethod.CASH,
                            subtotal = subtotal,
                            tax = tax,
                            total = total,
                            cashReceived = total,
                            cashChange = 0.0
                        )
                        val result = ReceiptPrinter.generateThermalReceiptPdf(context, effective, tx, items)
                        val file = File(result.pdfUri.path ?: return@OutlinedButton)
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(contentUri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val pm = context.packageManager
                        if (intent.resolveActivity(pm) != null) {
                            context.startActivity(intent)
                        } else {
                            id.stargan.intikasir.util.ShareUtils.shareUri(context, result.pdfUri, "application/pdf", "Bagikan Struk")
                        }
                    } catch (_: Exception) {
                    }
                },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Preview, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Preview Struk (Thermal)")
            }

            // Save button
            Button(
                onClick = {
                    val updated = settings.copy(
                        paperWidthMm = paperWidth,
                        paperCharPerLine = charPerLine,
                        printFormat = format,
                        autoCut = autoCut,
                        printLogo = printLogo,
                        useEscPosDirect = useEscPosDirect,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Simpan Pengaturan")
            }
        }
    }
}

