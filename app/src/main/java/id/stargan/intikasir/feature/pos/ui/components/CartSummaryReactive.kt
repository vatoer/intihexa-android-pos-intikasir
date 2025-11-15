package id.stargan.intikasir.feature.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.pos.ui.PosViewModelReactive

/**
 * Cart Summary for POS Screen
 * Uses OrderSummaryCard component for consistency
 */
@Composable
fun CartSummaryReactive(
    state: PosViewModelReactive.UiState,
    modifier: Modifier = Modifier
) {
    // Calculate item-level discount and gross subtotal
    val itemDiscountTotal = remember(state.transactionItems) {
        state.transactionItems.sumOf { it.discount }
    }
    val grossSubtotal = remember(state.transactionItems) {
        state.transactionItems.sumOf { it.unitPrice * it.quantity }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OrderSummaryCard(
                grossSubtotal = grossSubtotal,
                itemDiscount = itemDiscountTotal,
                netSubtotal = state.subtotal,
                taxRate = state.taxRate,
                taxAmount = state.tax,
                globalDiscount = state.globalDiscount,
                total = state.total,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


