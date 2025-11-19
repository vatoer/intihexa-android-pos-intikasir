package id.stargan.intikasir.feature.history.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.TransactionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import id.stargan.intikasir.data.local.entity.PaymentMethod
import java.util.UUID

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    currency: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(tx.transactionNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(dateFormatter.format(Date(tx.updatedAt)), style = MaterialTheme.typography.bodySmall)
                Text("Kasir: ${tx.cashierName}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(currency.format(tx.total).replace("Rp", "Rp "), style = MaterialTheme.typography.titleMedium)
                Text(tx.status.name, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// Preview data provider
private class TransactionRowPreviewProvider : PreviewParameterProvider<TransactionEntity> {
    override val values = sequenceOf(
        TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionNumber = "INV-2024-0001",
            transactionDate = System.currentTimeMillis(),
            cashierId = "cashier1",
            cashierName = "John Doe",
            subtotal = 150000.0,
            tax = 15000.0,
            discount = 0.0,
            total = 165000.0,
            paymentMethod = PaymentMethod.CASH,
            cashReceived = 200000.0,
            cashChange = 35000.0,
            status = id.stargan.intikasir.data.local.entity.TransactionStatus.PAID,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = UUID.randomUUID().toString(),
            transactionNumber = "INV-2024-0002",
            transactionDate = System.currentTimeMillis() - 86400000,
            cashierId = "cashier2",
            cashierName = "Jane Smith",
            subtotal = 2500000.0,
            tax = 250000.0,
            discount = 50000.0,
            total = 2700000.0,
            paymentMethod = PaymentMethod.QRIS,
            cashReceived = 0.0,
            cashChange = 0.0,
            status = id.stargan.intikasir.data.local.entity.TransactionStatus.COMPLETED,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 86400000
        )
    )
}

@Preview(name = "Transaction Row - Cash Payment", showBackground = true)
@Composable
private fun TransactionRowPreview() {
    MaterialTheme {
        TransactionRow(
            tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                transactionNumber = "INV-2024-0001",
                transactionDate = System.currentTimeMillis(),
                cashierId = "cashier1",
                cashierName = "John Doe",
                subtotal = 150000.0,
                tax = 15000.0,
                discount = 0.0,
                total = 165000.0,
                paymentMethod = PaymentMethod.CASH,
                cashReceived = 200000.0,
                cashChange = 35000.0,
                status = id.stargan.intikasir.data.local.entity.TransactionStatus.PAID,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 },
            dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")),
            onClick = {}
        )
    }
}

@Preview(name = "Transaction Row - Large Amount", showBackground = true)
@Composable
private fun TransactionRowLargeAmountPreview() {
    MaterialTheme {
        TransactionRow(
            tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                transactionNumber = "INV-2024-0999",
                transactionDate = System.currentTimeMillis(),
                cashierId = "cashier2",
                cashierName = "Jane Smith",
                subtotal = 15750000.0,
                tax = 1575000.0,
                discount = 250000.0,
                total = 17075000.0,
                paymentMethod = PaymentMethod.QRIS,
                cashReceived = 0.0,
                cashChange = 0.0,
                status = id.stargan.intikasir.data.local.entity.TransactionStatus.COMPLETED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply { maximumFractionDigits = 0 },
            dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")),
            onClick = {}
        )
    }
}
