package id.stargan.intikasir.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID
import android.content.Intent
import androidx.core.content.FileProvider
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.pos.print.ReceiptPrinter
import id.stargan.intikasir.feature.pos.print.ESCPosPrinter
import id.stargan.intikasir.util.ShareUtils
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import java.io.File

@Composable
fun PrintingSettingsSection(
    settings: StoreSettings,
    onSave: (StoreSettings) -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
    var editMode by remember { mutableStateOf(false) }
    LaunchedEffect(isEditable) { if (!isEditable) editMode = false }

    var paperWidth by rememberSaveable(settings.paperWidthMm) { mutableStateOf(settings.paperWidthMm) }
    var charPerLine by rememberSaveable(settings.paperCharPerLine) { mutableStateOf(settings.paperCharPerLine) }
    val format = "THERMAL" // lock to thermal only
    var autoCut by rememberSaveable(settings.autoCut) { mutableStateOf(settings.autoCut) }
    var printLogo by rememberSaveable(settings.printLogo) { mutableStateOf(settings.printLogo) }
    var useEscPosDirect by rememberSaveable(settings.useEscPosDirect) { mutableStateOf(settings.useEscPosDirect) }

    // Reset to settings when cancelled
    LaunchedEffect(editMode) {
        if (!editMode) {
            paperWidth = settings.paperWidthMm
            charPerLine = settings.paperCharPerLine
            autoCut = settings.autoCut
            printLogo = settings.printLogo
            useEscPosDirect = settings.useEscPosDirect
        }
    }

    // Sample preview data lines (monospaced)
    val previewItems = remember(paperWidth, charPerLine, printLogo) {
        val header = listOf(
            if (printLogo) "[LOGO]" else "",
            centerLine("TOKO CONTOH", charPerLine),
            centerLine("Jl. Mawar No. 1", charPerLine),
            dashLine(charPerLine)
        ).filter { it.isNotBlank() }
        val bodyItems = listOf(
            itemLine("Sabun Cuci", 2, 5000.0, 0.0, charPerLine),
            itemLine("Beras Super", 1, 72000.0, 2000.0, charPerLine),
            itemLine("Teh Celup", 3, 4500.0, 0.0, charPerLine)
        )
        val subtotalPreview = 2 * 5000.0 + 1 * 72000.0 + 3 * 4500.0
        val discountPreview = 2000.0
        val taxableBase = subtotalPreview - discountPreview
        val taxPreview = if (settings.taxEnabled) (settings.taxPercentage / 100.0) * taxableBase else 0.0
        val grandTotal = taxableBase + taxPreview
        val totals = listOf(
            dashLine(charPerLine),
            totalLine("Subtotal", subtotalPreview, charPerLine),
            totalLine("Diskon", discountPreview, charPerLine),
            totalLine("Pajak", taxPreview, charPerLine),
            dashLine(charPerLine),
            totalLine("TOTAL", grandTotal, charPerLine),
            centerLine("Terima kasih!", charPerLine)
        )
        (header + bodyItems + totals)
    }

    val context = LocalContext.current
    var isPrinting by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pengaturan Cetak", style = MaterialTheme.typography.titleMedium)
                if (!editMode) {
                    TextButton(onClick = { editMode = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
            HorizontalDivider()

            if (!editMode) {
                // Display mode - show current settings
                InfoRow("Format Cetak", "Thermal")
                InfoRow("Lebar Kertas", "${paperWidth}mm (≈${charPerLine} karakter/baris)")
                InfoRow("Auto Cut", if (autoCut) "Aktif" else "Nonaktif")
                InfoRow("Tampilkan Logo", if (printLogo) "Ya" else "Tidak")
                InfoRow("ESC/POS Bluetooth", if (useEscPosDirect) "Aktif" else "Nonaktif")

                // Mini Preview
                Text("Preview Mini (Thermal)", style = MaterialTheme.typography.titleSmall)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .width(if (paperWidth == 58) 180.dp else 260.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        previewItems.forEach { line ->
                            Text(
                                text = line,
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                maxLines = 1
                            )
                        }
                    }
                }
                Text(
                    text = "Preview ini hanya simulasi layout monospasi berdasarkan jumlah karakter per baris.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Test print button
                Button(
                    enabled = isEditable && !isPrinting,
                    onClick = {
                        isPrinting = true
                        try {
                            if (useEscPosDirect && !settings.printerAddress.isNullOrBlank()) {
                                val dummyTx = TransactionEntity(
                                    transactionNumber = "DEMO-${System.currentTimeMillis()}",
                                    cashierId = "demo",
                                    cashierName = "Demo",
                                    paymentMethod = PaymentMethod.CASH,
                                    subtotal = 0.0,
                                    tax = 0.0,
                                    total = 0.0,
                                    cashReceived = 0.0,
                                    cashChange = 0.0
                                )
                                ESCPosPrinter.printReceipt(
                                    context = context,
                                    settings = settings.copy(
                                        paperWidthMm = paperWidth,
                                        paperCharPerLine = charPerLine,
                                        printLogo = printLogo,
                                        autoCut = autoCut,
                                        useEscPosDirect = useEscPosDirect
                                    ),
                                    transaction = dummyTx,
                                    items = emptyList()
                                )
                            }
                        } catch (_: Exception) {
                        } finally { isPrinting = false }
                    }
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mencetak...")
                    } else {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Test Print")
                    }
                }
                Text(
                    text = "Test Print hanya berfungsi jika ESC/POS Bluetooth aktif dan printer terhubung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Edit mode - show controls
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
                Text(
                    text = if (paperWidth == 58) "58 mm ≈ 32 karakter per baris" else "80 mm ≈ 48 karakter per baris",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Options
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto Cut (jika didukung)")
                    Switch(
                        checked = autoCut,
                        onCheckedChange = { autoCut = it }
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tampilkan Logo di Struk")
                    Switch(
                        checked = printLogo,
                        onCheckedChange = { printLogo = it }
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cetak langsung ESC/POS (Bluetooth)")
                    Switch(
                        checked = useEscPosDirect,
                        onCheckedChange = { useEscPosDirect = it }
                    )
                }

                // Edit mode action buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { editMode = false }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
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
                        editMode = false
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Helpers -----------------------------------------------------------
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun centerLine(text: String, width: Int): String {
    if (text.length >= width) return text.take(width)
    val padding = (width - text.length) / 2
    return " ".repeat(padding) + text + " ".repeat(width - padding - text.length)
}
private fun dashLine(width: Int): String = "-".repeat(width)
private fun itemLine(name: String, qty: Int, unitPrice: Double, discountPerUnit: Double, width: Int): String {
    val effectiveUnit = unitPrice - discountPerUnit
    val total = effectiveUnit * qty
    val left = name.take(width - 10)
    val right = total.toInt().toString()
    val space = width - left.length - right.length
    return left + " ".repeat(space.coerceAtLeast(1)) + right
}
private fun totalLine(label: String, amount: Double, width: Int): String {
    val left = label.take(width - 8)
    val amtStr = amount.toInt().toString()
    val space = width - left.length - amtStr.length
    return left + " ".repeat(space.coerceAtLeast(0)) + amtStr
}
