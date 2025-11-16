package id.stargan.intikasir.feature.pos.domain

import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Create new empty draft transaction
    suspend fun createEmptyDraft(
        cashierId: String,
        cashierName: String
    ): String // returns transaction ID

    // Get transaction by ID with reactive updates
    fun getTransactionById(transactionId: String): Flow<TransactionEntity?>

    // Get transaction items by transaction ID
    fun getTransactionItems(transactionId: String): Flow<List<TransactionItemEntity>>

    // Update transaction items (cart changes)
    suspend fun updateTransactionItems(
        transactionId: String,
        items: List<Pair<String, Int>>, // productId to quantity
        itemDiscounts: Map<String, Double> = emptyMap() // productId to discount
    )

    // Update transaction totals
    suspend fun updateTransactionTotals(
        transactionId: String,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double
    )

    // Update transaction payment method and discount
    suspend fun updateTransactionPayment(
        transactionId: String,
        paymentMethod: PaymentMethod,
        globalDiscount: Double
    )

    // Finalize transaction (mark as COMPLETED)
    suspend fun finalizeTransaction(
        transactionId: String,
        cashReceived: Double,
        cashChange: Double,
        notes: String?
    )

    // Create a new transaction
    suspend fun createTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>,
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        cashReceived: Double,
        cashChange: Double,
        notes: String?,
        status: TransactionStatus
    ): String

    // Create a draft transaction
    suspend fun createDraftTransaction(
        cashierId: String,
        cashierName: String,
        items: List<Pair<String, Int>>,
        paymentMethod: PaymentMethod,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        notes: String?
    ): String

    // History: list transactions by date range (inclusive), only completed by default
    fun getTransactionsByDateRange(startDate: Long, endDate: Long, onlyCompleted: Boolean = true): Flow<List<TransactionEntity>>

    // History: list transactions on an exact day (local)
    fun getTransactionsByDate(date: Long): Flow<List<TransactionEntity>>

    // All non-deleted transactions
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Soft delete a transaction (admin only in UI)
    suspend fun softDeleteTransaction(transactionId: String)
}
