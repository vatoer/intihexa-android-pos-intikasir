package id.stargan.intikasir.feature.reports.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.components.SummaryCards
import id.stargan.intikasir.feature.reports.ui.components.RevenueExpenseTrendChart
import id.stargan.intikasir.feature.reports.ui.components.TopProductsCard
import id.stargan.intikasir.feature.reports.ui.components.PaymentMethodBreakdownCard
import id.stargan.intikasir.feature.reports.ui.components.ExpenseCategoryBreakdownCard
import id.stargan.intikasir.feature.reports.ui.components.PeriodInfoCard
import id.stargan.intikasir.feature.reports.ui.components.NetProfitCard
import id.stargan.intikasir.feature.reports.ui.components.RevenueBreakdownCard
import id.stargan.intikasir.feature.reports.ui.components.ExpenseBreakdownCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportsEvent.DismissError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportsEvent.DismissSuccess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Period selector
                    TextButton(
                        onClick = { viewModel.onEvent(ReportsEvent.ShowPeriodPicker) }
                    ) {
                        Text(getPeriodLabel(uiState.selectedPeriod))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    // Refresh
                    IconButton(onClick = { viewModel.onEvent(ReportsEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    // Export
                    IconButton(onClick = { viewModel.onEvent(ReportsEvent.ShowExportDialog) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            PrimaryTabRow(
                selectedTabIndex = when (uiState.selectedTab) {
                    ReportTab.DASHBOARD -> 0
                    ReportTab.PROFIT_LOSS -> 1
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == ReportTab.DASHBOARD,
                    onClick = { viewModel.onEvent(ReportsEvent.SelectTab(ReportTab.DASHBOARD)) },
                    text = { Text("Dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == ReportTab.PROFIT_LOSS,
                    onClick = { viewModel.onEvent(ReportsEvent.SelectTab(ReportTab.PROFIT_LOSS)) },
                    text = { Text("Laba Rugi") },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
                )
            }

            // Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.selectedTab) {
                    ReportTab.DASHBOARD -> {
                        uiState.dashboard?.let { dashboard ->
                            DashboardContent(dashboard = dashboard)
                        }
                    }
                    ReportTab.PROFIT_LOSS -> {
                        uiState.profitLossReport?.let { report ->
                            ProfitLossContent(report = report)
                        }
                    }
                }
            }
        }
    }

    // Period Picker Dialog
    if (uiState.showPeriodPicker) {
        PeriodPickerDialog(
            currentPeriod = uiState.selectedPeriod,
            onPeriodSelected = { period ->
                viewModel.onEvent(ReportsEvent.SelectPeriod(period))
            },
            onDismiss = { viewModel.onEvent(ReportsEvent.HidePeriodPicker) }
        )
    }

    // Export Dialog
    if (uiState.showExportDialog) {
        ExportDialog(
            onExportCSV = {
                scope.launch {
                    try {
                        // TODO: Implement export
                        snackbarHostState.showSnackbar("Fitur export akan segera tersedia")
                        viewModel.onEvent(ReportsEvent.HideExportDialog)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Gagal export: ${e.message}")
                    }
                }
            },
            onExportPDF = {
                scope.launch {
                    snackbarHostState.showSnackbar("Export PDF akan segera tersedia")
                    viewModel.onEvent(ReportsEvent.HideExportDialog)
                }
            },
            onDismiss = { viewModel.onEvent(ReportsEvent.HideExportDialog) }
        )
    }
}

@Composable
private fun DashboardContent(
    dashboard: id.stargan.intikasir.feature.reports.domain.model.ReportDashboard,
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

        // Revenue & Expense Trend Chart
        if (dashboard.dailyRevenue.isNotEmpty()) {
            RevenueExpenseTrendChart(
                revenueData = dashboard.dailyRevenue,
                expenseData = dashboard.dailyExpense
            )
        }

        // Top Products
        if (dashboard.topProducts.isNotEmpty()) {
            TopProductsCard(products = dashboard.topProducts)
        }

        // Payment Method Breakdown
        if (dashboard.paymentMethodBreakdown.isNotEmpty()) {
            PaymentMethodBreakdownCard(data = dashboard.paymentMethodBreakdown)
        }

        // Expense Category Breakdown
        if (dashboard.expenseCategoryBreakdown.isNotEmpty()) {
            ExpenseCategoryBreakdownCard(data = dashboard.expenseCategoryBreakdown)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfitLossContent(
    report: id.stargan.intikasir.feature.reports.domain.model.ProfitLossReport,
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

@Composable
private fun PeriodPickerDialog(
    currentPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Periode") },
        text = {
            Column {
                PeriodType.entries.filter { it != PeriodType.CUSTOM }.forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentPeriod == period,
                            onClick = { onPeriodSelected(period) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getPeriodLabel(period))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun ExportDialog(
    onExportCSV: () -> Unit,
    onExportPDF: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Laporan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih format export:")

                OutlinedButton(
                    onClick = onExportCSV,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export ke CSV (Excel)")
                }

                OutlinedButton(
                    onClick = onExportPDF,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export ke PDF")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun getPeriodLabel(period: PeriodType): String {
    return when (period) {
        PeriodType.TODAY -> "Hari Ini"
        PeriodType.YESTERDAY -> "Kemarin"
        PeriodType.THIS_WEEK -> "Minggu Ini"
        PeriodType.LAST_WEEK -> "Minggu Lalu"
        PeriodType.THIS_MONTH -> "Bulan Ini"
        PeriodType.LAST_MONTH -> "Bulan Lalu"
        PeriodType.THIS_YEAR -> "Tahun Ini"
        PeriodType.CUSTOM -> "Custom"
    }
}

