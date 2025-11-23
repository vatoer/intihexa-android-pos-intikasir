package id.stargan.intikasir.feature.reports.domain.repository

import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.feature.reports.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {

    /**
     * Get dashboard data untuk overview
     */
    suspend fun getDashboardData(startDate: Long, endDate: Long, cashierId: String? = null): ReportDashboard

    /**
     * Get detailed transaction report
     */
    suspend fun getTransactionReport(filter: ReportFilter): TransactionReport

    /**
     * Get expense report
     */
    suspend fun getExpenseReport(filter: ReportFilter): ExpenseReport

    /**
     * Get profit & loss report
     */
    suspend fun getProfitLossReport(startDate: Long, endDate: Long): ProfitLossReport

    /**
     * Get daily revenue trend
     */
    suspend fun getDailyRevenueTrend(startDate: Long, endDate: Long): List<DailyData>

    /**
     * Get daily expense trend
     */
    suspend fun getDailyExpenseTrend(startDate: Long, endDate: Long): List<DailyData>

    /**
     * Get top selling products
     */
    suspend fun getTopSellingProducts(startDate: Long, endDate: Long, cashierId: String? = null, limit: Int = 10): List<ProductSales>

    /**
     * Get worst selling products report
     * - soldButLow: products that have sales but <= lowThreshold quantity
     * - notSold: products with zero sales in the period
     */
    suspend fun getWorstSellingProducts(startDate: Long, endDate: Long, cashierId: String? = null, lowThreshold: Int = 5): WorstProductsReport

    /**
     * Get payment method breakdown
     */
    suspend fun getPaymentMethodBreakdown(startDate: Long, endDate: Long): List<PaymentMethodData>

    /**
     * Get expense category breakdown
     */
    suspend fun getExpenseCategoryBreakdown(startDate: Long, endDate: Long): List<ExpenseCategoryData>

    /**
     * Get all transactions in date range
     */
    fun getTransactions(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * Get all expenses in date range
     */
    fun getExpenses(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    /**
     * Get transaction items for transactions
     */
    suspend fun getTransactionItems(transactionIds: List<String>): Map<String, List<TransactionItemEntity>>
}
