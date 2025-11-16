package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId AND isDeleted = 0")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE id = :expenseId AND isDeleted = 0")
    fun getExpenseByIdFlow(expenseId: String): Flow<ExpenseEntity?>

    @Query("""
        SELECT * FROM expenses 
        WHERE date >= :startDate AND date <= :endDate 
        AND isDeleted = 0 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE DATE(date / 1000, 'unixepoch', 'localtime') = DATE(:date / 1000, 'unixepoch', 'localtime')
        AND isDeleted = 0 
        ORDER BY createdAt DESC
    """)
    fun getExpensesByDate(date: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE category = :category 
        AND isDeleted = 0 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE date >= :startDate AND date <= :endDate 
        AND isDeleted = 0
    """)
    suspend fun getTotalExpenses(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE DATE(date / 1000, 'unixepoch', 'localtime') = DATE(:date / 1000, 'unixepoch', 'localtime')
        AND isDeleted = 0
    """)
    suspend fun getDailyTotal(date: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isDeleted = 1, updatedAt = :timestamp WHERE id = :expenseId")
    suspend fun softDeleteExpense(expenseId: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

