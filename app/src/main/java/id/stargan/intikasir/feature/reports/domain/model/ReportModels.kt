package id.stargan.intikasir.feature.reports.domain.model

import id.stargan.intikasir.data.local.entity.PaymentMethod
import id.stargan.intikasir.data.local.entity.ExpenseCategory

/**
 * Data model untuk dashboard laporan
 */
data class ReportDashboard(
    // Summary metrics
    val totalRevenue: Double,
    val totalExpense: Double,
    val netProfit: Double,
    val transactionCount: Int,
    
    // Trend data (untuk grafik)
    val dailyRevenue: List<DailyData>,
    val dailyExpense: List<DailyData>,
    
    // Top products
    val topProducts: List<ProductSales>,
    
    // Payment method breakdown
    val paymentMethodBreakdown: List<PaymentMethodData>,
    
    // Expense category breakdown
    val expenseCategoryBreakdown: List<ExpenseCategoryData>,
    
    // Period info
    val periodStart: Long,
    val periodEnd: Long
)

data class DailyData(
    val date: Long,
    val amount: Double,
    val count: Int = 0
)

data class ProductSales(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Double
)

data class ProductInfo(
    val productId: String,
    val productName: String,
    val stock: Int
)

data class WorstProductsReport(
    val worstProducts: List<ProductSales>, // products with smallest sold quantity (ascending), limit 10
    val notSold: List<ProductInfo> // products with zero sales
)

data class PaymentMethodData(
    val method: PaymentMethod,
    val amount: Double,
    val count: Int,
    val percentage: Double
)

data class ExpenseCategoryData(
    val category: ExpenseCategory,
    val amount: Double,
    val count: Int,
    val percentage: Double
)

/**
 * Detailed transaction report
 */
data class TransactionReport(
    val transactions: List<TransactionReportItem>,
    val summary: TransactionSummary
)

data class TransactionReportItem(
    val transactionNumber: String,
    val date: Long,
    val cashierName: String,
    val paymentMethod: PaymentMethod,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val itemCount: Int
)

data class TransactionSummary(
    val totalTransactions: Int,
    val totalRevenue: Double,
    val averageTransaction: Double,
    val totalDiscount: Double,
    val totalTax: Double
)

/**
 * Expense report
 */
data class ExpenseReport(
    val expenses: List<ExpenseReportItem>,
    val summary: ExpenseSummary
)

data class ExpenseReportItem(
    val date: Long,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val createdBy: String
)

data class ExpenseSummary(
    val totalExpense: Double,
    val totalCount: Int,
    val averageExpense: Double,
    val byCategory: Map<ExpenseCategory, Double>
)

/**
 * Profit & Loss Report
 */
data class ProfitLossReport(
    val revenue: RevenueBreakdown,
    val expenses: ExpenseBreakdown,
    val netProfit: Double,
    val profitMargin: Double, // Percentage
    val periodStart: Long,
    val periodEnd: Long
)

data class RevenueBreakdown(
    val grossSales: Double,
    val discounts: Double,
    val netSales: Double,
    val tax: Double
)

data class ExpenseBreakdown(
    val operational: Double,
    val inventory: Double,
    val salary: Double,
    val utilities: Double,
    val maintenance: Double,
    val marketing: Double,
    val other: Double,
    val total: Double
)

/**
 * Filter untuk report
 */
data class ReportFilter(
    val startDate: Long,
    val endDate: Long,
    val periodType: PeriodType = PeriodType.CUSTOM,
    val cashierId: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val expenseCategory: ExpenseCategory? = null
)

enum class PeriodType {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM
}
