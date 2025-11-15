package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.pos.domain.model.CartItem
import id.stargan.intikasir.ui.common.Stepper
import id.stargan.intikasir.ui.common.LeftButtonMode
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PosProductItem(
    product: Product,
    cartItem: CartItem?,
    onAdd: () -> Unit,
    onChangeQty: (Int) -> Unit,
    onSetDiscount: ((Double) -> Unit)? = null,
    modifier: Modifier = Modifier
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nf.format(product.price).replace("Rp", "Rp "), style = MaterialTheme.typography.labelMedium)
                if (cartItem?.itemDiscount != null && cartItem.itemDiscount > 0) {
                    Text(
                        "Diskon: ${nf.format(cartItem.itemDiscount).replace("Rp", "Rp ")}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (product.stock <= (product.lowStockThreshold ?: 10)) {
                    Text("Stok menipis", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (cartItem == null) {
                Button(onClick = onAdd, enabled = product.stock > 0) { Text("Tambah") }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onSetDiscount != null) {
                        IconButton(onClick = { showDiscountDialog = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Diskon")
                        }
                    }
                    Stepper(
                        value = cartItem.quantity,
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

    if (showDiscountDialog && cartItem != null && onSetDiscount != null) {
        var discountText by remember { mutableStateOf(cartItem.itemDiscount.toInt().toString()) }
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
