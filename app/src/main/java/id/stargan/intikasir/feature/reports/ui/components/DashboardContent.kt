package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.ReportDashboard

@Composable
fun DashboardContent(
    dashboard: ReportDashboard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        SummaryCards(
            revenue = dashboard.totalRevenue,
            expense = dashboard.totalExpense,
            profit = dashboard.netProfit,
            transactionCount = dashboard.transactionCount
        )

        // Revenue & Expense Trend Line Chart (Vico)
        if (dashboard.dailyRevenue.isNotEmpty() || dashboard.dailyExpense.isNotEmpty()) {
            RevenueExpenseTrendLineChart(
                revenueData = dashboard.dailyRevenue,
                expenseData = dashboard.dailyExpense
            )
        }

        // Top Products
        if (dashboard.topProducts.isNotEmpty()) {
            TopProductsCard(products = dashboard.topProducts)
        }

        // Payment Method Pie Chart (Visual)
        if (dashboard.paymentMethodBreakdown.isNotEmpty()) {
            PaymentMethodPieChart(data = dashboard.paymentMethodBreakdown)
        }

        // Expense Category Bar Chart (Visual)
        if (dashboard.expenseCategoryBreakdown.isNotEmpty()) {
            ExpenseCategoryBarChart(data = dashboard.expenseCategoryBreakdown)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

