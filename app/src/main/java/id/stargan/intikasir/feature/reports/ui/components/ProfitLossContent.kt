package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.ProfitLossReport

@Composable
fun ProfitLossContent(
    report: ProfitLossReport,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Info
        PeriodInfoCard(
            startDate = report.periodStart,
            endDate = report.periodEnd
        )

        // Net Profit Summary
        NetProfitCard(
            netProfit = report.netProfit,
            profitMargin = report.profitMargin
        )

        // Revenue Breakdown
        RevenueBreakdownCard(revenue = report.revenue)

        // Expense Breakdown
        ExpenseBreakdownCard(expenses = report.expenses)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

