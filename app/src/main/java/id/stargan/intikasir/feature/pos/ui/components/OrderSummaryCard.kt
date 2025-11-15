package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Reusable Order Summary Card Component
 * Menampilkan ringkasan pesanan yang sama di POS dan Payment screen
 */
@Composable
fun OrderSummaryCard(
    grossSubtotal: Double,
    itemDiscount: Double,
    netSubtotal: Double,
    taxRate: Double,
    taxAmount: Double,
    globalDiscount: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    val nf = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Ringkasan Pesanan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider()

            // Gross subtotal (before item discount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal (bruto)", style = MaterialTheme.typography.bodySmall)
                Text(
                    nf.format(grossSubtotal).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Item-level discount
            if (itemDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Diskon item", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "-${nf.format(itemDiscount).replace("Rp", "Rp ")}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Net subtotal (after item discount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Subtotal",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    nf.format(netSubtotal).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            // Tax (PPN)
            if (taxRate > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "PPN (${(taxRate * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        nf.format(taxAmount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Global discount
            if (globalDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Diskon global", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "-${nf.format(globalDiscount).replace("Rp", "Rp ")}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider()

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    nf.format(total).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

