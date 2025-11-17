package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.DailyData
import id.stargan.intikasir.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Revenue & Expense Trend Chart - Simplified Version
 * Using simple bar visualization instead of complex Vico chart
 */
@Composable
fun RevenueExpenseTrendLineChart(
    revenueData: List<DailyData>,
    expenseData: List<DailyData>,
    modifier: Modifier = Modifier
) {
    val incomeColor = MaterialTheme.colorScheme.extendedColors.incomeColor
    val expenseColor = MaterialTheme.colorScheme.extendedColors.expenseColor

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tren Pendapatan & Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (revenueData.isEmpty() && expenseData.isEmpty()) {
                Text(
                    text = "Belum ada data untuk periode ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                // Simple summary instead of complex chart
                val totalRevenue = revenueData.sumOf { it.amount }
                val totalExpense = expenseData.sumOf { it.amount }
                val maxValue = maxOf(totalRevenue, totalExpense)

                // Revenue bar
                SimpleTrendBar(
                    label = "Pendapatan",
                    amount = totalRevenue,
                    maxValue = maxValue,
                    color = incomeColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Expense bar
                SimpleTrendBar(
                    label = "Pengeluaran",
                    amount = totalExpense,
                    maxValue = maxValue,
                    color = expenseColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary text
                Text(
                    text = "${revenueData.size + expenseData.size} data points dalam periode ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SimpleTrendBar(
    label: String,
    amount: Double,
    maxValue: Double,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((amount / maxValue).toFloat())
                    .fillMaxHeight()
                    .background(
                        color = color,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

/**
 * Payment Method Distribution Chart
 */
@Composable
fun PaymentMethodPieChart(
    data: List<id.stargan.intikasir.feature.reports.domain.model.PaymentMethodData>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribusi Metode Pembayaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "Belum ada data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                data.forEach { item ->
                    PaymentBar(
                        label = getPaymentName(item.method),
                        percentage = item.percentage.toFloat(),
                        color = getPaymentColor(item.method)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PaymentBar(label: String, percentage: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Expense Category Bar Chart
 */
@Composable
fun ExpenseCategoryBarChart(
    data: List<id.stargan.intikasir.feature.reports.domain.model.ExpenseCategoryData>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribusi Kategori Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text(
                    text = "Belum ada data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                data.take(5).forEach { item ->
                    ExpenseBar(
                        label = getCategoryName(item.category),
                        percentage = item.percentage.toFloat(),
                        amount = item.amount
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseBar(label: String, percentage: Float, amount: Double) {
    val expenseColor = MaterialTheme.colorScheme.extendedColors.expenseColor
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.weight(1f).height(12.dp),
                color = expenseColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                style = MaterialTheme.typography.bodySmall,
                color = expenseColor
            )
        }
    }
}

// Helper functions
private fun getPaymentName(method: id.stargan.intikasir.data.local.entity.PaymentMethod): String {
    return when (method) {
        id.stargan.intikasir.data.local.entity.PaymentMethod.CASH -> "Tunai"
        id.stargan.intikasir.data.local.entity.PaymentMethod.QRIS -> "QRIS"
        id.stargan.intikasir.data.local.entity.PaymentMethod.CARD -> "Kartu"
        id.stargan.intikasir.data.local.entity.PaymentMethod.TRANSFER -> "Transfer"
    }
}

@Composable
private fun getPaymentColor(method: id.stargan.intikasir.data.local.entity.PaymentMethod): Color {
    return when (method) {
        id.stargan.intikasir.data.local.entity.PaymentMethod.CASH -> MaterialTheme.colorScheme.primary
        id.stargan.intikasir.data.local.entity.PaymentMethod.QRIS -> MaterialTheme.colorScheme.secondary
        id.stargan.intikasir.data.local.entity.PaymentMethod.CARD -> MaterialTheme.colorScheme.tertiary
        id.stargan.intikasir.data.local.entity.PaymentMethod.TRANSFER -> MaterialTheme.colorScheme.extendedColors.info
    }
}

private fun getCategoryName(category: id.stargan.intikasir.data.local.entity.ExpenseCategory): String {
    return when (category) {
        id.stargan.intikasir.data.local.entity.ExpenseCategory.SUPPLIES -> "Perlengkapan"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.UTILITIES -> "Utilitas"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.RENT -> "Sewa"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.SALARY -> "Gaji"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.MARKETING -> "Marketing"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.MAINTENANCE -> "Perawatan"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.TRANSPORT -> "Transportasi"
        id.stargan.intikasir.data.local.entity.ExpenseCategory.MISC -> "Lain-lain"
    }
}

private fun formatAmount(amount: Double): String {
    val millions = amount / 1_000_000
    return when {
        millions >= 1 -> String.format(Locale.getDefault(), "Rp %.1f jt", millions)
        amount >= 1_000 -> String.format(Locale.getDefault(), "Rp %.0f rb", amount / 1_000)
        else -> String.format(Locale.getDefault(), "Rp %.0f", amount)
    }
}

