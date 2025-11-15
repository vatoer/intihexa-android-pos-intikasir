package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.stargan.intikasir.feature.pos.ui.PosViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart

@Composable
fun PayButton(
    state: PosViewModel.UiState,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nf = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    val label = "Bayar • " + nf.format(state.total).replace("Rp", "Rp ")
    ExtendedFloatingActionButton(
        onClick = onPay,
        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
        text = { Text(label) },
        modifier = modifier,
        expanded = true
    )
}
