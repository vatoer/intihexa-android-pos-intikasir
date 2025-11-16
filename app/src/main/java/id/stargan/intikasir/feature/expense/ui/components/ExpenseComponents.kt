package id.stargan.intikasir.feature.expense.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reusable expense item card
 */
@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale("id", "ID")) }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Category icon & details
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category icon
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = getCategoryIcon(expense.category),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Details
                Column {
                    Text(
                        text = getCategoryLabel(expense.category),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "${dateFormat.format(Date(expense.date))} • ${expense.paymentMethod.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Amount
            Text(
                text = currency.format(expense.amount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Category summary card
 */
@Composable
fun CategorySummaryCard(
    category: ExpenseCategory,
    amount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = getCategoryLabel(category),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = currency.format(amount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Daily summary card
 */
@Composable
fun DailySummaryCard(
    date: Long,
    total: Double,
    count: Int,
    modifier: Modifier = Modifier
) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("id", "ID")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Pengeluaran",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = dateFormat.format(Date(date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currency.format(total).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "$count transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// Helper functions
fun getCategoryIcon(category: ExpenseCategory) = when (category) {
    ExpenseCategory.SUPPLIES -> Icons.Default.ShoppingCart
    ExpenseCategory.UTILITIES -> Icons.Default.Lightbulb
    ExpenseCategory.RENT -> Icons.Default.Home
    ExpenseCategory.SALARY -> Icons.Default.AccountBalance
    ExpenseCategory.MARKETING -> Icons.Default.Campaign
    ExpenseCategory.MAINTENANCE -> Icons.Default.Build
    ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
    ExpenseCategory.MISC -> Icons.Default.MoreHoriz
}

fun getCategoryLabel(category: ExpenseCategory) = when (category) {
    ExpenseCategory.SUPPLIES -> "Perlengkapan"
    ExpenseCategory.UTILITIES -> "Utilitas"
    ExpenseCategory.RENT -> "Sewa"
    ExpenseCategory.SALARY -> "Gaji"
    ExpenseCategory.MARKETING -> "Marketing"
    ExpenseCategory.MAINTENANCE -> "Perbaikan"
    ExpenseCategory.TRANSPORT -> "Transport"
    ExpenseCategory.MISC -> "Lain-lain"
}

