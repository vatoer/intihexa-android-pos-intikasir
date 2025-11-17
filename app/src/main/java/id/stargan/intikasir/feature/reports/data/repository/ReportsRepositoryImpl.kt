package id.stargan.intikasir.feature.reports.data.repository

import id.stargan.intikasir.data.local.dao.TransactionDao
import id.stargan.intikasir.data.local.dao.TransactionItemDao
import id.stargan.intikasir.data.local.dao.ExpenseDao
import id.stargan.intikasir.data.local.entity.*
import id.stargan.intikasir.feature.reports.domain.model.*
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao,
    private val expenseDao: ExpenseDao
) : ReportsRepository {

    override suspend fun getDashboardData(startDate: Long, endDate: Long): ReportDashboard {
        val revenue = transactionDao.getTotalRevenue(startDate, endDate) ?: 0.0
        val expense = expenseDao.getTotalExpenses(startDate, endDate) ?: 0.0
        val transactionCount = transactionDao.getTransactionCount(startDate, endDate)

        return ReportDashboard(
            totalRevenue = revenue,
            totalExpense = expense,
            netProfit = revenue - expense,
            transactionCount = transactionCount,
            dailyRevenue = getDailyRevenueTrend(startDate, endDate),
            dailyExpense = getDailyExpenseTrend(startDate, endDate),
            topProducts = getTopSellingProducts(startDate, endDate, 5),
            paymentMethodBreakdown = getPaymentMethodBreakdown(startDate, endDate),
            expenseCategoryBreakdown = getExpenseCategoryBreakdown(startDate, endDate),
            periodStart = startDate,
            periodEnd = endDate
        )
    }

    override suspend fun getTransactionReport(filter: ReportFilter): TransactionReport {
        val transactions = transactionDao.getTransactionsByDateRangeAllStatus(
            filter.startDate,
            filter.endDate
        ).first()

        // Filter by payment method if specified
        val filtered = if (filter.paymentMethod != null) {
            transactions.filter { it.paymentMethod == filter.paymentMethod }
        } else {
            transactions
        }.filter { it.status == TransactionStatus.COMPLETED }

        val items = filtered.map { tx ->
            val itemCount = transactionItemDao.getItemsByTransactionSuspend(tx.id).size
            TransactionReportItem(
                transactionNumber = tx.transactionNumber,
                date = tx.transactionDate,
                cashierName = tx.cashierName,
                paymentMethod = tx.paymentMethod,
                subtotal = tx.subtotal,
                tax = tx.tax,
                discount = tx.discount,
                total = tx.total,
                itemCount = itemCount
            )
        }

        val totalRevenue = filtered.sumOf { it.total }
        val totalDiscount = filtered.sumOf { it.discount }
        val totalTax = filtered.sumOf { it.tax }

        val summary = TransactionSummary(
            totalTransactions = filtered.size,
            totalRevenue = totalRevenue,
            averageTransaction = if (filtered.isNotEmpty()) totalRevenue / filtered.size else 0.0,
            totalDiscount = totalDiscount,
            totalTax = totalTax
        )

        return TransactionReport(items, summary)
    }

    override suspend fun getExpenseReport(filter: ReportFilter): ExpenseReport {
        val expenses = expenseDao.getExpensesByDateRange(filter.startDate, filter.endDate).first()

        // Filter by category if specified
        val filtered = if (filter.expenseCategory != null) {
            expenses.filter { it.category == filter.expenseCategory }
        } else {
            expenses
        }

        val items = filtered.map { exp ->
            ExpenseReportItem(
                date = exp.date,
                category = exp.category,
                description = exp.description,
                amount = exp.amount,
                createdBy = exp.createdByName
            )
        }

        val byCategory = filtered.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val total = filtered.sumOf { it.amount }

        val summary = ExpenseSummary(
            totalExpense = total,
            totalCount = filtered.size,
            averageExpense = if (filtered.isNotEmpty()) total / filtered.size else 0.0,
            byCategory = byCategory
        )

        return ExpenseReport(items, summary)
    }

    override suspend fun getProfitLossReport(startDate: Long, endDate: Long): ProfitLossReport {
        val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()
        val expenses = expenseDao.getExpensesByDateRange(startDate, endDate).first()

        val grossSales = transactions.sumOf { it.subtotal }
        val discounts = transactions.sumOf { it.discount }
        val netSales = grossSales - discounts
        val tax = transactions.sumOf { it.tax }

        val revenue = RevenueBreakdown(
            grossSales = grossSales,
            discounts = discounts,
            netSales = netSales,
            tax = tax
        )

        val expenseByCategory = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount } }

        val expenseBreakdown = ExpenseBreakdown(
            operational = (expenseByCategory[ExpenseCategory.SUPPLIES] ?: 0.0) +
                         (expenseByCategory[ExpenseCategory.RENT] ?: 0.0),
            inventory = 0.0, // Not tracked separately in current schema
            salary = expenseByCategory[ExpenseCategory.SALARY] ?: 0.0,
            utilities = expenseByCategory[ExpenseCategory.UTILITIES] ?: 0.0,
            maintenance = expenseByCategory[ExpenseCategory.MAINTENANCE] ?: 0.0,
            marketing = expenseByCategory[ExpenseCategory.MARKETING] ?: 0.0,
            other = (expenseByCategory[ExpenseCategory.TRANSPORT] ?: 0.0) +
                   (expenseByCategory[ExpenseCategory.MISC] ?: 0.0),
            total = expenses.sumOf { it.amount }
        )

        val totalRevenue = netSales + tax
        val netProfit = totalRevenue - expenseBreakdown.total
        val profitMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

        return ProfitLossReport(
            revenue = revenue,
            expenses = expenseBreakdown,
            netProfit = netProfit,
            profitMargin = profitMargin,
            periodStart = startDate,
            periodEnd = endDate
        )
    }

    override suspend fun getDailyRevenueTrend(startDate: Long, endDate: Long): List<DailyData> {
        val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()

        // Group by day
        val calendar = Calendar.getInstance()
        val dailyMap = mutableMapOf<String, MutableList<TransactionEntity>>()

        transactions.forEach { tx ->
            calendar.timeInMillis = tx.transactionDate
            val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyMap.getOrPut(dayKey) { mutableListOf() }.add(tx)
        }

        // Convert to DailyData
        return dailyMap.map { (_, txList) ->
            val firstTx = txList.first()
            calendar.timeInMillis = firstTx.transactionDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            DailyData(
                date = calendar.timeInMillis,
                amount = txList.sumOf { it.total },
                count = txList.size
            )
        }.sortedBy { it.date }
    }

    override suspend fun getDailyExpenseTrend(startDate: Long, endDate: Long): List<DailyData> {
        val expenses = expenseDao.getExpensesByDateRange(startDate, endDate).first()

        // Group by day
        val calendar = Calendar.getInstance()
        val dailyMap = mutableMapOf<String, MutableList<ExpenseEntity>>()

        expenses.forEach { exp ->
            calendar.timeInMillis = exp.date
            val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyMap.getOrPut(dayKey) { mutableListOf() }.add(exp)
        }

        // Convert to DailyData
        return dailyMap.map { (_, expList) ->
            val firstExp = expList.first()
            calendar.timeInMillis = firstExp.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            DailyData(
                date = calendar.timeInMillis,
                amount = expList.sumOf { it.amount },
                count = expList.size
            )
        }.sortedBy { it.date }
    }

    override suspend fun getTopSellingProducts(startDate: Long, endDate: Long, limit: Int): List<ProductSales> {
        val topItems = transactionItemDao.getTopSellingProducts(startDate, endDate, limit)

        return topItems.map { item ->
            ProductSales(
                productId = item.productId,
                productName = item.productName,
                quantitySold = item.quantity,
                revenue = item.subtotal
            )
        }
    }

    override suspend fun getPaymentMethodBreakdown(startDate: Long, endDate: Long): List<PaymentMethodData> {
        val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()

        val grouped = transactions.groupBy { it.paymentMethod }
        val total = transactions.sumOf { it.total }

        return grouped.map { (method, txList) ->
            val amount = txList.sumOf { it.total }
            PaymentMethodData(
                method = method,
                amount = amount,
                count = txList.size,
                percentage = if (total > 0) (amount / total) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }
    }

    override suspend fun getExpenseCategoryBreakdown(startDate: Long, endDate: Long): List<ExpenseCategoryData> {
        val expenses = expenseDao.getExpensesByDateRange(startDate, endDate).first()

        val grouped = expenses.groupBy { it.category }
        val total = expenses.sumOf { it.amount }

        return grouped.map { (category, expList) ->
            val amount = expList.sumOf { it.amount }
            ExpenseCategoryData(
                category = category,
                amount = amount,
                count = expList.size,
                percentage = if (total > 0) (amount / total) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }
    }

    override fun getTransactions(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    override fun getExpenses(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    override suspend fun getTransactionItems(transactionIds: List<String>): Map<String, List<TransactionItemEntity>> {
        val result = mutableMapOf<String, List<TransactionItemEntity>>()
        transactionIds.forEach { txId ->
            val items = transactionItemDao.getItemsByTransactionSuspend(txId)
            result[txId] = items
        }
        return result
    }
}

