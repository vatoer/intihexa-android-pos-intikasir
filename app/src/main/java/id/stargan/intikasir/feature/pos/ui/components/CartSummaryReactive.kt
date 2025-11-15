package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartSummaryReactive(
    state: PosViewModelReactive.UiState,
    modifier: Modifier = Modifier
) {
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Item")
                Text(state.totalQuantity.toString())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal")
                Text(nf.format(state.subtotal).replace("Rp", "Rp "))
            }
            if (state.taxRate > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pajak (${(state.taxRate * 100).toInt()}%)")
                    Text(nf.format(state.tax).replace("Rp", "Rp "))
                }
            }
            if (state.globalDiscount > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Diskon")
                    Text(
                        "-${nf.format(state.globalDiscount).replace("Rp", "Rp ")}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    nf.format(state.total).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

