package id.stargan.intikasir.feature.home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

/**
 * Reusable card component that displays a brief summary of today's sales.
 * Params are nullable so the caller can pass null while data is loading or unavailable.
 */
@Composable
fun SalesSummaryCard(
    modifier: Modifier = Modifier,
    totalSales: Long?,
    transactionCount: Int?,
    netChange: Long? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ringkasan Penjualan Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoading -> {
                        // Simple loading state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Memuat...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    !errorMessage.isNullOrBlank() -> {
                        // Error state with optional retry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (onRetry != null) {
                                IconButton(onClick = onRetry) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                                }
                            }
                        }
                    }

                    else -> {
                        // Normal display
                        Text(
                            text = formatRupiah(totalSales),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaksi: ${transactionCount?.toString() ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            netChange?.let { change ->
                                val color = when {
                                    change > 0L -> Color(0xFF2E7D32) // greenish
                                    change < 0L -> Color(0xFFB00020) // reddish
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val sign = if (change > 0L) "+" else if (change < 0L) "-" else ""
                                Text(
                                    text = "${sign}${formatRupiah(kotlin.math.abs(change))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color
                                )
                            } ?: run {
                                Text(
                                    text = "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format a nullable Long amount into Indonesian Rupiah string without decimals.
 * Returns "-" when amount is null.
 */
fun formatRupiah(amount: Long?): String {
    if (amount == null) return "-"
    val locale = Locale.forLanguageTag("id-ID")
    val nf = NumberFormat.getCurrencyInstance(locale).apply { maximumFractionDigits = 0 }
    return nf.format(amount)
}

@Preview(showBackground = true)
@Composable
fun SalesSummaryCardPreview() {
    MaterialTheme {
        SalesSummaryCard(totalSales = 1250000L, transactionCount = 12, netChange = 150000L)
    }
}

@Preview(showBackground = true)
@Composable
fun SalesSummaryCardLoadingPreview() {
    MaterialTheme {
        SalesSummaryCard(totalSales = null, transactionCount = null, isLoading = true)
    }
}

@Preview(showBackground = true)
@Composable
fun SalesSummaryCardErrorPreview() {
    MaterialTheme {
        SalesSummaryCard(totalSales = null, transactionCount = null, errorMessage = "Gagal memuat", onRetry = {})
    }
}
