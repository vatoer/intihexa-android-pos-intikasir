package id.stargan.intikasir.feature.history.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ItemDetailRow(
    name: String,
    quantity: Int,
    productPrice: Double,
    unitPrice: Double,
    discount: Double,
    subtotal: Double,
    currency: NumberFormat,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (discount > 0) {
                val origPriceStr = currency.format(productPrice).replace("Rp", "Rp ")
                Text("@${origPriceStr}/pcs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val discountPerUnit = discount / quantity
                val discountedPricePerUnit = productPrice - discountPerUnit
                val discountedStr = currency.format(discountedPricePerUnit).replace("Rp", "Rp ")
                val subStr = currency.format(subtotal).replace("Rp", "Rp ")
                Text("${quantity} x ${discountedStr} = ${subStr}", style = MaterialTheme.typography.bodySmall)
                val discountStr = currency.format(discount).replace("Rp", "Rp ")
                val discountPerUnitStr = currency.format(discountPerUnit).replace("Rp", "Rp ")
                Text(
                    "Diskon: ${discountPerUnitStr}/pcs (Total: -${discountStr})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                val unitStr = currency.format(unitPrice).replace("Rp", "Rp ")
                val subStr = currency.format(subtotal).replace("Rp", "Rp ")
                Text("${quantity} x ${unitStr} = ${subStr}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(name = "Item Detail - No Discount", showBackground = true)
@Composable
private fun ItemDetailRowPreview() {
    MaterialTheme {
        ItemDetailRow(
            name = "Sabun Mandi",
            quantity = 2,
            productPrice = 15000.0,
            unitPrice = 15000.0,
            discount = 0.0,
            subtotal = 30000.0,
            currency = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 }
        )
    }
}

@Preview(name = "Item Detail - With Discount", showBackground = true)
@Composable
private fun ItemDetailRowWithDiscountPreview() {
    MaterialTheme {
        ItemDetailRow(
            name = "Shampo Anti Ketombe 500ml",
            quantity = 3,
            productPrice = 45000.0,
            unitPrice = 42000.0,
            discount = 9000.0,
            subtotal = 126000.0,
            currency = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 }
        )
    }
}

@Preview(name = "Item Detail - Large Quantity", showBackground = true)
@Composable
private fun ItemDetailRowLargeQuantityPreview() {
    MaterialTheme {
        ItemDetailRow(
            name = "Pulpen Biru",
            quantity = 25,
            productPrice = 2500.0,
            unitPrice = 2000.0,
            discount = 12500.0,
            subtotal = 50000.0,
            currency = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 }
        )
    }
}
