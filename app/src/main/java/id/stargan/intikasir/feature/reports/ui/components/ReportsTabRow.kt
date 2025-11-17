package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import id.stargan.intikasir.feature.reports.ui.ReportTab

@Composable
fun ReportsTabRow(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = when (selectedTab) {
            ReportTab.DASHBOARD -> 0
            ReportTab.PROFIT_LOSS -> 1
        }
    ) {
        Tab(
            selected = selectedTab == ReportTab.DASHBOARD,
            onClick = { onTabSelected(ReportTab.DASHBOARD) },
            text = { Text("Dashboard") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null
                )
            }
        )
        Tab(
            selected = selectedTab == ReportTab.PROFIT_LOSS,
            onClick = { onTabSelected(ReportTab.PROFIT_LOSS) },
            text = { Text("Laba Rugi") },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null
                )
            }
        )
    }
}

