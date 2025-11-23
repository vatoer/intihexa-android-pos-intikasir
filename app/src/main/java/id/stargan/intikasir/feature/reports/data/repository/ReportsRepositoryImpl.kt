package id.stargan.intikasir.feature.reports.data.repository

import id.stargan.intikasir.data.local.dao.TransactionDao
import id.stargan.intikasir.data.local.dao.TransactionItemDao
import id.stargan.intikasir.data.local.dao.ExpenseDao
import id.stargan.intikasir.data.local.dao.ProductDao
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.feature.reports.domain.model.DailyData
import id.stargan.intikasir.feature.reports.domain.model.ExpenseCategoryData
import id.stargan.intikasir.feature.reports.domain.model.ProductSales
import id.stargan.intikasir.feature.reports.domain.model.PaymentMethodData
import id.stargan.intikasir.feature.reports.domain.model.ReportDashboard
import id.stargan.intikasir.feature.reports.domain.model.ReportFilter
import id.stargan.intikasir.feature.reports.domain.model.TransactionReport
import id.stargan.intikasir.feature.reports.domain.model.TransactionReportItem
import id.stargan.intikasir.feature.reports.domain.model.TransactionSummary
import id.stargan.intikasir.feature.reports.domain.model.ExpenseReport
import id.stargan.intikasir.feature.reports.domain.model.ExpenseReportItem
import id.stargan.intikasir.feature.reports.domain.model.ExpenseSummary
import id.stargan.intikasir.feature.reports.domain.model.ProfitLossReport
import id.stargan.intikasir.feature.reports.domain.model.RevenueBreakdown
import id.stargan.intikasir.feature.reports.domain.model.ExpenseBreakdown
import id.stargan.intikasir.feature.reports.domain.model.WorstProductsReport
import id.stargan.intikasir.feature.reports.domain.model.ProductInfo
import id.stargan.intikasir.feature.reports.domain.repository.ReportsRepository
import id.stargan.intikasir.data.local.entity.ExpenseCategory as LocalExpenseCategory
import id.stargan.intikasir.data.local.entity.TransactionStatus as LocalTransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class ReportsRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao,
    private val expenseDao: ExpenseDao,
    private val productDao: ProductDao
) : ReportsRepository {

    override suspend fun getDashboardData(startDate: Long, endDate: Long, cashierId: String?): ReportDashboard {
        // Fetch transactions and expenses, then apply cashier filter if provided
        val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()
        val filteredTransactions = if (!cashierId.isNullOrBlank()) {
            transactions.filter { it.cashierId == cashierId }
        } else {
            transactions
        }

        val revenue = filteredTransactions.sumOf { it.total }
        // Fetch all expenses for range and filter by cashier if requested
        val allExpenses = expenseDao.getExpensesByDateRange(startDate, endDate).first()
        val filteredExpenses = if (!cashierId.isNullOrBlank()) {
            allExpenses.filter { it.createdBy == cashierId }
        } else {
            allExpenses
        }
        val expense = filteredExpenses.sumOf { it.amount }
         // Note: expenses are global; if you want cashier-specific expenses tracked, adapt later
         val transactionCount = filteredTransactions.size

         // Compute dailyRevenue by grouping filteredTransactions by day (midnight timestamp)
         val calendar = Calendar.getInstance()
         val dailyMap = mutableMapOf<Long, MutableList<TransactionEntity>>()
         for (tx in filteredTransactions) {
             calendar.timeInMillis = tx.transactionDate
             calendar.set(Calendar.HOUR_OF_DAY, 0)
             calendar.set(Calendar.MINUTE, 0)
             calendar.set(Calendar.SECOND, 0)
             calendar.set(Calendar.MILLISECOND, 0)
             val dayKey = calendar.timeInMillis
             dailyMap.getOrPut(dayKey) { mutableListOf() }.add(tx)
         }

         val dailyRevenueList = dailyMap.map { (date, txList) ->
             DailyData(
                 date = date,
                 amount = txList.sumOf { it.total },
                 count = txList.size
             )
         }.sortedBy { it.date }

         // Compute dailyExpense by grouping filteredExpenses by day
         val dailyExpenseMap = mutableMapOf<Long, MutableList<ExpenseEntity>>()
         for (exp in filteredExpenses) {
             calendar.timeInMillis = exp.date
             calendar.set(Calendar.HOUR_OF_DAY, 0)
             calendar.set(Calendar.MINUTE, 0)
             calendar.set(Calendar.SECOND, 0)
             calendar.set(Calendar.MILLISECOND, 0)
             val dayKey = calendar.timeInMillis
             dailyExpenseMap.getOrPut(dayKey) { mutableListOf() }.add(exp)
         }

         val dailyExpenseList = dailyExpenseMap.map { (date, expList) ->
             DailyData(
                 date = date,
                 amount = expList.sumOf { it.amount },
                 count = expList.size
             )
         }.sortedBy { it.date }

         // Compute expenseCategoryBreakdown from filtered expenses
         val expenseTotal = filteredExpenses.sumOf { it.amount }
         val expenseByCategory = filteredExpenses.groupBy { it.category }
             .map { (category, list) ->
                 ExpenseCategoryData(
                     category = category,
                     amount = list.sumOf { it.amount },
                     count = list.size,
                     percentage = if (expenseTotal > 0) (list.sumOf { it.amount } / expenseTotal) * 100 else 0.0
                 )
             }.sortedByDescending { it.amount }

        // Compute topProducts using SQL aggregation in DAO
        val topProducts = if (_isEmpty(filteredTransactions = filteredTransactions)) {
            emptyList()
        } else {
            val rows = if (!cashierId.isNullOrBlank()) {
                transactionItemDao.getTopSellingProductsByRangeAndCashier(startDate, endDate, cashierId, 10)
            } else {
                transactionItemDao.getTopSellingProductsByRange(startDate, endDate, 10)
            }
            rows.map { ProductSales(it.productId, it.productName, it.totalQuantity, it.totalRevenue) }
        }

        // Compute paymentMethodBreakdown from filteredTransactions
        val paymentGrouped = filteredTransactions.groupBy { it.paymentMethod }
        val totalForPayment = filteredTransactions.sumOf { it.total }
        val paymentMethodBreakdown = paymentGrouped.map { (method, txList) ->
            val amount = txList.sumOf { it.total }
            PaymentMethodData(
                method = method,
                amount = amount,
                count = txList.size,
                percentage = if (totalForPayment > 0) (amount / totalForPayment) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }

        return ReportDashboard(
            totalRevenue = revenue,
            totalExpense = expense,
            netProfit = revenue - expense,
            transactionCount = transactionCount,
            dailyRevenue = dailyRevenueList,
            dailyExpense = dailyExpenseList,
            topProducts = topProducts,
            paymentMethodBreakdown = paymentMethodBreakdown,
            expenseCategoryBreakdown = expenseByCategory,
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
        }.filter { it.status == LocalTransactionStatus.COMPLETED }

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
            operational = (expenseByCategory[LocalExpenseCategory.SUPPLIES] ?: 0.0) +
                         (expenseByCategory[LocalExpenseCategory.RENT] ?: 0.0),
            inventory = 0.0, // Not tracked separately in current schema
            salary = expenseByCategory[LocalExpenseCategory.SALARY] ?: 0.0,
            utilities = expenseByCategory[LocalExpenseCategory.UTILITIES] ?: 0.0,
            maintenance = expenseByCategory[LocalExpenseCategory.MAINTENANCE] ?: 0.0,
            marketing = expenseByCategory[LocalExpenseCategory.MARKETING] ?: 0.0,
            other = (expenseByCategory[LocalExpenseCategory.TRANSPORT] ?: 0.0) +
                   (expenseByCategory[LocalExpenseCategory.MISC] ?: 0.0),
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

    override suspend fun getTopSellingProducts(startDate: Long, endDate: Long, cashierId: String?, limit: Int): List<ProductSales> {
        // Use DAO aggregated query for performance
        val rows = if (!cashierId.isNullOrBlank()) {
            transactionItemDao.getTopSellingProductsByRangeAndCashier(startDate, endDate, cashierId, limit)
        } else {
            transactionItemDao.getTopSellingProductsByRange(startDate, endDate, limit)
        }
        return rows.map { ProductSales(it.productId, it.productName, it.totalQuantity, it.totalRevenue) }
    }

    override suspend fun getWorstSellingProducts(startDate: Long, endDate: Long, cashierId: String?, lowThreshold: Int): WorstProductsReport {
        // Aggregate sales per product from filtered transactions
        val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()
        val filteredTransactions = if (!cashierId.isNullOrBlank()) transactions.filter { it.cashierId == cashierId } else transactions

        val transactionIds = filteredTransactions.map { it.id }
        val items = if (transactionIds.isNotEmpty()) transactionItemDao.getItemsByTransactionIds(transactionIds) else emptyList()

        val salesByProduct = items.groupBy { it.productId }.mapValues { it.value.sumOf { it.quantity } }

        // Get all active products
        val allProducts = productDao.getAllActiveProducts().first()

        // Build list of products with sold quantity > 0
        val soldProducts = salesByProduct.mapNotNull { (productId, qty) ->
            val p = allProducts.find { it.id == productId }
            p?.let { ProductSales(productId, it.name, qty, items.filter { it.productId == productId }.sumOf { it.subtotal }) }
        }

        // worstProducts: take products with sold qty > 0, sorted ascending by qty, limit 10
        val worstProducts = soldProducts.sortedBy { it.quantitySold }.take(10)

        // notSold: all active products not in salesByProduct
        val notSold = allProducts.filter { p -> !salesByProduct.containsKey(p.id) }
            .map { ProductInfo(productId = it.id, productName = it.name, stock = it.stock) }

        return WorstProductsReport(worstProducts = worstProducts, notSold = notSold)
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

    private data class ProductSalesBuilder(
        val productId: String,
        val productName: String,
        var quantitySold: Int = 0,
        var revenue: Double = 0.0
    )

    // Small helper to check empty filteredTransactions without extra DB calls
    private fun _isEmpty(filteredTransactions: List<TransactionEntity>): Boolean = filteredTransactions.isEmpty()
}
