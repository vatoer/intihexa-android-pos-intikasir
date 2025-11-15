package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Text(
                    nf.format(product.price).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.labelMedium
                )

                // Show discount if applied
                if (transactionItem != null && transactionItem.discount > 0) {
                    Text(
                        "Diskon: ${nf.format(transactionItem.discount).replace("Rp", "Rp ")}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Show subtotal if qty > 1
                if (transactionItem != null && transactionItem.quantity > 1) {
                    Text(
//                        "Subtotal: ${transactionItem.quantity} x ${nf.format(product.price).replace("Rp", "Rp ")} = ${nf.format(transactionItem.subtotal).replace("Rp", "Rp ")}",
                        " x ${transactionItem.quantity} = ${nf.format(transactionItem.subtotal).replace("Rp", "Rp ")}",
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

    // Discount dialog
    if (showDiscountDialog && transactionItem != null && onSetDiscount != null) {
        var discountText by remember { mutableStateOf(transactionItem.discount.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showDiscountDialog = false },
            title = { Text("Diskon Item") },
            text = {
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah Diskon") },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val discount = discountText.toDoubleOrNull() ?: 0.0
                    onSetDiscount(discount)
                    showDiscountDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscountDialog = false }) { Text("Batal") }
            }
        )
    }
}

