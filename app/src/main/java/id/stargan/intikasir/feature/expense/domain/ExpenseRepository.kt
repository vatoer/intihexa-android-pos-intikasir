package id.stargan.intikasir.feature.expense.domain

import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>
    fun getExpensesByDate(date: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<ExpenseEntity>>

    suspend fun createExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expenseId: String)

    suspend fun getDailyTotal(date: Long): Double
    suspend fun getTotalExpenses(startDate: Long, endDate: Long): Double
}

