package id.stargan.intikasir.domain.model

/**
 * Domain model untuk Transaction
 */
data class Transaction(
    val id: String,
    val transactionNumber: String,
    val transactionDate: Long,
    val cashierId: String,
    val cashierName: String,
    val paymentMethod: PaymentMethod,
    val subtotal: Double,
    val tax: Double = 0.0,
    val service: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double,
    val cashReceived: Double = 0.0,
    val cashChange: Double = 0.0,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val notes: String? = null,
    val items: List<TransactionItem> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
) {
    fun formattedTotal(): String = formatCurrency(total)
    fun formattedSubtotal(): String = formatCurrency(subtotal)
    fun formattedTax(): String = formatCurrency(tax)
    fun formattedService(): String = formatCurrency(service)

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }
}

/**
 * Domain model untuk Transaction Item
 */
data class TransactionItem(
    val id: String,
    val transactionId: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productSku: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val subtotal: Double,
    val notes: String? = null,
    val createdAt: Long
)

enum class PaymentMethod {
    CASH,
    QRIS,
    CARD,
    TRANSFER;

    fun displayName(): String = when (this) {
        CASH -> "Tunai"
        QRIS -> "QRIS"
        CARD -> "Kartu"
        TRANSFER -> "Transfer"
    }
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    REFUNDED;

    fun displayName(): String = when (this) {
        PENDING -> "Pending"
        COMPLETED -> "Selesai"
        CANCELLED -> "Dibatalkan"
        REFUNDED -> "Dikembalikan"
    }
}

