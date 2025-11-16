package id.stargan.intikasir.feature.expense.data

import id.stargan.intikasir.data.local.dao.ExpenseDao
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.ExpenseCategory
import id.stargan.intikasir.feature.expense.domain.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    override fun getExpenseById(expenseId: String): Flow<ExpenseEntity?> {
        return expenseDao.getExpenseByIdFlow(expenseId)
    }

    override fun getExpensesByDate(date: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDate(date)
    }

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(category)
    }

    override suspend fun createExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteExpense(expenseId: String) = withContext(Dispatchers.IO) {
        expenseDao.softDeleteExpense(expenseId)
    }

    override suspend fun getDailyTotal(date: Long): Double = withContext(Dispatchers.IO) {
        expenseDao.getDailyTotal(date) ?: 0.0
    }

    override suspend fun getTotalExpenses(startDate: Long, endDate: Long): Double = withContext(Dispatchers.IO) {
        expenseDao.getTotalExpenses(startDate, endDate) ?: 0.0
    }
}

