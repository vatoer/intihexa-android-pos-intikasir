package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransactionByIdFlow(transactionId: String): Flow<TransactionEntity?>

    @Query("""
        SELECT * FROM transactions 
        WHERE transactionDate BETWEEN :startDate AND :endDate 
        AND isDeleted = 0 
        AND status = 'COMPLETED'
        ORDER BY transactionDate DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE DATE(transactionDate / 1000, 'unixepoch', 'localtime') = DATE(:date / 1000, 'unixepoch', 'localtime')
        AND isDeleted = 0 
        AND status = 'COMPLETED'
        ORDER BY transactionDate DESC
    """)
    fun getTransactionsByDate(date: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE cashierId = :cashierId AND isDeleted = 0 ORDER BY transactionDate DESC")
    fun getTransactionsByCashier(cashierId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status AND isDeleted = 0 ORDER BY transactionDate DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(total) FROM transactions 
        WHERE transactionDate BETWEEN :startDate AND :endDate 
        AND status = 'COMPLETED'
        AND isDeleted = 0
    """)
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE transactionDate BETWEEN :startDate AND :endDate 
        AND status = 'COMPLETED'
        AND isDeleted = 0
    """)
    suspend fun getTransactionCount(startDate: Long, endDate: Long): Int

    @Query("SELECT MAX(transactionNumber) FROM transactions WHERE transactionNumber LIKE :prefix || '%'")
    suspend fun getLastTransactionNumber(prefix: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status, updatedAt = :timestamp WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :timestamp WHERE id = :transactionId")
    suspend fun softDeleteTransaction(transactionId: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET syncedAt = :timestamp WHERE id = :transactionId")
    suspend fun markAsSynced(transactionId: String, timestamp: Long)

    @Query("SELECT * FROM transactions WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    // New methods for reactive POS
    @Query("""
        UPDATE transactions 
        SET subtotal = :subtotal, tax = :tax, discount = :discount, total = :total, updatedAt = :timestamp 
        WHERE id = :transactionId
    """)
    suspend fun updateTransactionTotals(
        transactionId: String,
        subtotal: Double,
        tax: Double,
        discount: Double,
        total: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE transactions 
        SET paymentMethod = :paymentMethod, discount = :globalDiscount, updatedAt = :timestamp 
        WHERE id = :transactionId
    """)
    suspend fun updateTransactionPayment(
        transactionId: String,
        paymentMethod: id.stargan.intikasir.data.local.entity.PaymentMethod,
        globalDiscount: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE transactions 
        SET cashReceived = :cashReceived, cashChange = :cashChange, notes = :notes, status = :status, updatedAt = :timestamp 
        WHERE id = :transactionId
    """)
    suspend fun finalizeTransaction(
        transactionId: String,
        cashReceived: Double,
        cashChange: Double,
        notes: String?,
        status: TransactionStatus,
        timestamp: Long = System.currentTimeMillis()
    )
}
