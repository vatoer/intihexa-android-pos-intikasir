package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.feature.reports.domain.model.*
import id.stargan.intikasir.ui.theme.extendedColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Summary Cards untuk metrik utama
 */
@Composable
fun SummaryCards(
    revenue: Double,
    expense: Double,
    profit: Double,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Revenue Card
            MetricCard(
                title = "Pendapatan",
                value = currency.format(revenue).replace("Rp", "Rp "),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                containerColor = MaterialTheme.colorScheme.extendedColors.incomeColor,
                modifier = Modifier.weight(1f)
            )

            // Expense Card
            MetricCard(
                title = "Pengeluaran",
                value = currency.format(expense).replace("Rp", "Rp "),
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                containerColor = MaterialTheme.colorScheme.extendedColors.expenseColor,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profit Card
            MetricCard(
                title = "Laba Bersih",
                value = currency.format(profit).replace("Rp", "Rp "),
                icon = Icons.Default.Savings,
                containerColor = if (profit >= 0) {
                    MaterialTheme.colorScheme.extendedColors.netProfitColor
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.weight(1f)
            )

            // Transaction Count Card
            MetricCard(
                title = "Transaksi",
                value = "$transactionCount",
                icon = Icons.Default.Receipt,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = containerColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = containerColor
            )
        }
    }
}

/**
 * Revenue & Expense Trend Chart (Simple bar chart)
 */
@Suppress("UNUSED_PARAMETER") // Will be used when chart library is integrated
@Composable
fun RevenueExpenseTrendChart(
    revenueData: List<DailyData>,
    expenseData: List<DailyData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tren Pendapatan & Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Simple text-based chart for now
            // TODO: Integrate proper charting library (MPAndroidChart or Vico)
            Text(
                text = "Grafik akan ditampilkan di sini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            )

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.extendedColors.incomeColor,
                    label = "Pendapatan"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.extendedColors.expenseColor,
                    label = "Pengeluaran"
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = MaterialTheme.shapes.small
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Top Products Card
 */
@Composable
fun TopProductsCard(
    products: List<ProductSales>,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Produk Terlaris",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.productName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${product.quantitySold} terjual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = currency.format(product.revenue).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (product != products.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Payment Method Breakdown Card
 */
@Composable
fun PaymentMethodBreakdownCard(
    data: List<PaymentMethodData>,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Metode Pembayaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            data.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getPaymentMethodName(item.method),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${item.count} transaksi (${String.format("%.1f", item.percentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = currency.format(item.amount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (item != data.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Expense Category Breakdown Card
 */
@Composable
fun ExpenseCategoryBreakdownCard(
    data: List<ExpenseCategoryData>,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kategori Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            data.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getCategoryLabel(item.category),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${item.count} transaksi (${String.format("%.1f", item.percentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = currency.format(item.amount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.extendedColors.expenseColor
                    )
                }
                if (item != data.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Period Info Card
 */
@Composable
fun PeriodInfoCard(
    startDate: Long,
    endDate: Long,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Periode Laporan",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Net Profit Card
 */
@Composable
fun NetProfitCard(
    netProfit: Double,
    profitMargin: Double,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    val isProfit = netProfit >= 0
    val cardColor = if (isProfit) {
        MaterialTheme.colorScheme.extendedColors.successContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isProfit) "Laba Bersih" else "Rugi Bersih",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currency.format(kotlin.math.abs(netProfit)).replace("Rp", "Rp "),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Margin: ${String.format("%.2f", kotlin.math.abs(profitMargin))}%",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Revenue Breakdown Card
 */
@Composable
fun RevenueBreakdownCard(
    revenue: RevenueBreakdown,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rincian Pendapatan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            BreakdownRow("Penjualan Kotor", currency.format(revenue.grossSales).replace("Rp", "Rp "))
            BreakdownRow("Diskon", "(${currency.format(revenue.discounts).replace("Rp", "Rp ")})", isNegative = true)
            HorizontalDivider()
            BreakdownRow("Penjualan Bersih", currency.format(revenue.netSales).replace("Rp", "Rp "), isBold = true)
            BreakdownRow("PPN", currency.format(revenue.tax).replace("Rp", "Rp "))
            HorizontalDivider(thickness = 2.dp)
            BreakdownRow(
                "Total Pendapatan",
                currency.format(revenue.netSales + revenue.tax).replace("Rp", "Rp "),
                isBold = true,
                color = MaterialTheme.colorScheme.extendedColors.incomeColor
            )
        }
    }
}

/**
 * Expense Breakdown Card
 */
@Composable
fun ExpenseBreakdownCard(
    expenses: ExpenseBreakdown,
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rincian Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            BreakdownRow("Operasional", currency.format(expenses.operational).replace("Rp", "Rp "))
            BreakdownRow("Inventori", currency.format(expenses.inventory).replace("Rp", "Rp "))
            BreakdownRow("Gaji", currency.format(expenses.salary).replace("Rp", "Rp "))
            BreakdownRow("Utilitas", currency.format(expenses.utilities).replace("Rp", "Rp "))
            BreakdownRow("Perawatan", currency.format(expenses.maintenance).replace("Rp", "Rp "))
            BreakdownRow("Marketing", currency.format(expenses.marketing).replace("Rp", "Rp "))
            BreakdownRow("Lain-lain", currency.format(expenses.other).replace("Rp", "Rp "))
            HorizontalDivider(thickness = 2.dp)
            BreakdownRow(
                "Total Pengeluaran",
                currency.format(expenses.total).replace("Rp", "Rp "),
                isBold = true,
                color = MaterialTheme.colorScheme.extendedColors.expenseColor
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isNegative: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isNegative) MaterialTheme.colorScheme.error else color
        )
    }
}

private fun getPaymentMethodName(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.CASH -> "Tunai"
        PaymentMethod.QRIS -> "QRIS"
        PaymentMethod.CARD -> "Kartu Debit/Kredit"
        PaymentMethod.TRANSFER -> "Transfer Bank"
    }
}

private fun getCategoryLabel(category: id.stargan.intikasir.data.local.entity.ExpenseCategory): String {
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

