package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    fun getItemsByTransaction(transactionId: String): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    suspend fun getItemsByTransactionSuspend(transactionId: String): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): TransactionItemEntity?

    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT transaction_items.*, SUM(quantity) as totalQuantity 
        FROM transaction_items 
        INNER JOIN transactions ON transaction_items.transactionId = transactions.id
        WHERE transactions.transactionDate BETWEEN :startDate AND :endDate
        AND transactions.status = 'COMPLETED'
        AND transactions.isDeleted = 0
        GROUP BY productId
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int = 10): List<TransactionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TransactionItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Update
    suspend fun updateItem(item: TransactionItemEntity)

    @Delete
    suspend fun deleteItem(item: TransactionItemEntity)

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsByTransaction(transactionId: String)

    // Additional methods for reactive POS
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    fun getItemsByTransactionIdFlow(transactionId: String): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    suspend fun getItemsByTransactionId(transactionId: String): List<TransactionItemEntity>

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsByTransactionId(transactionId: String)

    // Batch fetch items by transaction ids
    @Query("SELECT * FROM transaction_items WHERE transactionId IN (:transactionIds)")
    suspend fun getItemsByTransactionIds(transactionIds: List<String>): List<TransactionItemEntity>

    // Projection for aggregated product sales
    data class ProductSalesRow(
        val productId: String,
        val productName: String,
        val totalQuantity: Int,
        val totalRevenue: Double
    )

    // Top selling products in date range (all cashiers)
    @Query("""
        SELECT ti.productId as productId, ti.productName as productName, SUM(ti.quantity) as totalQuantity, SUM(ti.subtotal) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        WHERE t.transactionDate BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED' AND t.isDeleted = 0
        GROUP BY ti.productId
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProductsByRange(startDate: Long, endDate: Long, limit: Int = 10): List<ProductSalesRow>

    // Top selling products in date range for specific cashier
    @Query("""
        SELECT ti.productId as productId, ti.productName as productName, SUM(ti.quantity) as totalQuantity, SUM(ti.subtotal) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        WHERE t.transactionDate BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED' AND t.isDeleted = 0
        AND t.cashierId = :cashierId
        GROUP BY ti.productId
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProductsByRangeAndCashier(startDate: Long, endDate: Long, cashierId: String, limit: Int = 10): List<ProductSalesRow>

    // Worst selling products (least quantity > 0) in date range (all cashiers)
    @Query("""
        SELECT ti.productId as productId, ti.productName as productName, SUM(ti.quantity) as totalQuantity, SUM(ti.subtotal) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        WHERE t.transactionDate BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED' AND t.isDeleted = 0
        GROUP BY ti.productId
        HAVING SUM(ti.quantity) > 0
        ORDER BY totalQuantity ASC
        LIMIT :limit
    """)
    suspend fun getWorstSellingProductsByRange(startDate: Long, endDate: Long, limit: Int = 10): List<ProductSalesRow>

    // Worst selling products for specific cashier
    @Query("""
        SELECT ti.productId as productId, ti.productName as productName, SUM(ti.quantity) as totalQuantity, SUM(ti.subtotal) as totalRevenue
        FROM transaction_items ti
        INNER JOIN transactions t ON ti.transactionId = t.id
        WHERE t.transactionDate BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED' AND t.isDeleted = 0
        AND t.cashierId = :cashierId
        GROUP BY ti.productId
        HAVING SUM(ti.quantity) > 0
        ORDER BY totalQuantity ASC
        LIMIT :limit
    """)
    suspend fun getWorstSellingProductsByRangeAndCashier(startDate: Long, endDate: Long, cashierId: String, limit: Int = 10): List<ProductSalesRow>
}
