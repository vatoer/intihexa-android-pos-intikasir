package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.pos.ui.PosViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartSummary(
    state: PosViewModel.UiState,
    modifier: Modifier = Modifier,
    onClear: () -> Unit
) {
    val nf = rememberRupiah()
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Keranjang", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (state.cartItems.isNotEmpty()) {
                    TextButton(onClick = onClear) { Text("Kosongkan") }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Item")
                Text(state.totalQuantity.toString())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal")
                Text(nf.format(state.subtotal).replace("Rp", "Rp "))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pajak")
                Text(nf.format(state.tax).replace("Rp", "Rp "))
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(nf.format(state.total).replace("Rp", "Rp "), style = MaterialTheme.typography.titleMedium)
            }
            if (state.cartItems.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.cartItems, key = { it.productId }) { item ->
                        AssistChip(onClick = {}, label = { Text(item.name) })
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberRupiah(): NumberFormat {
    return NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
}
