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
}
