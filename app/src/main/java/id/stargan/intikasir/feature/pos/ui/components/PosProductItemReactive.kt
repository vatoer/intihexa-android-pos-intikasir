package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.ui.common.Stepper
import id.stargan.intikasir.ui.common.LeftButtonMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PosProductItemReactive(
    modifier: Modifier = Modifier,
    product: Product,
    transactionItem: TransactionItemEntity?,
    onAdd: () -> Unit,
    onChangeQty: (Int) -> Unit,
    onSetDiscount: ((Double) -> Unit)? = null,
) {
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    var showDiscountDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .defaultMinSize(minHeight = 80.dp)
            ,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Show original price (always)
                if (transactionItem != null && transactionItem.discount > 0) {
                    // Show original price with strikethrough indicator
                    Text(
                        "@${nf.format(product.price).replace("Rp", "Rp ")}/pcs",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Calculate and show discounted price per unit
                    val discountPerUnit = transactionItem.discount / transactionItem.quantity
                    val discountedPricePerUnit = product.price - discountPerUnit
                    Text(
                        nf.format(discountedPricePerUnit).replace("Rp", "Rp ") + "/pcs",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        nf.format(product.price).replace("Rp", "Rp ") + "/pcs",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Show total discount if applied
                if (transactionItem != null && transactionItem.discount > 0) {
                    val discountPerUnit = transactionItem.discount / transactionItem.quantity
                    Text(
                        "Diskon: ${nf.format(discountPerUnit).replace("Rp", "Rp ")}/pcs (Total: ${nf.format(transactionItem.discount).replace("Rp", "Rp ")})",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Show subtotal with quantity
                if (transactionItem != null && transactionItem.quantity > 0) {
                    val discountPerUnit = if (transactionItem.discount > 0) {
                        transactionItem.discount / transactionItem.quantity
                    } else 0.0
                    val priceAfterDiscount = product.price - discountPerUnit

                    Text(
                        "${transactionItem.quantity} x ${nf.format(priceAfterDiscount).replace("Rp", "Rp ")} = ${nf.format(transactionItem.subtotal).replace("Rp", "Rp ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Low stock warning
                if (product.stock <= (product.lowStockThreshold ?: 10)) {
                    Text(
                        "Stok menipis",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (transactionItem == null) {
                Button(onClick = onAdd, enabled = product.stock > 0) {
                    Text("Tambah")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onSetDiscount != null) {
                        IconButton(onClick = { showDiscountDialog = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Diskon")
                        }
                    }
                    Stepper(
                        value = transactionItem.quantity,
                        onValueChange = onChangeQty,
                        min = 0,
                        max = product.stock,
                        leftButtonMode = LeftButtonMode.Delete,
                        onDelete = { onChangeQty(0) },
                        step = 1
                    )
                }
            }
        }
    }

    // Discount dialog - Opsi B: Input diskon PER PCS
    if (showDiscountDialog && transactionItem != null && onSetDiscount != null) {
        // Calculate current discount per unit
        val currentDiscountPerUnit = if (transactionItem.discount > 0) {
            (transactionItem.discount / transactionItem.quantity).toInt()
        } else 0

        var discountPerUnitText by remember { mutableStateOf(currentDiscountPerUnit.toString()) }
        val discountPerUnit = discountPerUnitText.toIntOrNull() ?: 0
        val totalDiscount = discountPerUnit * transactionItem.quantity
        val maxDiscountPerUnit = product.price.toInt()

        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Diskon per Pcs") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Info produk
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                product.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Harga: ${nf.format(product.price).replace("Rp", "Rp ")}/pcs",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Quantity: ${transactionItem.quantity} pcs",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Input diskon per pcs
                    OutlinedTextField(
                        value = discountPerUnitText,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() }
                            discountPerUnitText = filtered
                        },
                        label = { Text("Diskon per Pcs") },
                        prefix = { Text("Rp ") },
                        suffix = { Text("/pcs") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            Text(
                                "Maks: ${nf.format(maxDiscountPerUnit).replace("Rp", "Rp ")}/pcs",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        isError = discountPerUnit > maxDiscountPerUnit
                    )

                    // Preview
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Preview:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            val priceAfterDiscount = product.price - discountPerUnit
                            val subtotal = priceAfterDiscount * transactionItem.quantity

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Harga asli:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${nf.format(product.price).replace("Rp", "Rp ")}/pcs",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (discountPerUnit > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Diskon per pcs:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "-${nf.format(discountPerUnit).replace("Rp", "Rp ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Harga jadi:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${nf.format(priceAfterDiscount).replace("Rp", "Rp ")}/pcs",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total diskon:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "-${nf.format(totalDiscount).replace("Rp", "Rp ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    nf.format(subtotal).replace("Rp", "Rp "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (discountPerUnit <= maxDiscountPerUnit) {
                            // Send per-unit discount (ViewModel will multiply by quantity)
                            onSetDiscount(discountPerUnit.toDouble())
                            showDiscountDialog = false
                        }
                    },
                    enabled = discountPerUnit <= maxDiscountPerUnit
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscountDialog = false }) { Text("Batal") }
            }
        )
    }
}

