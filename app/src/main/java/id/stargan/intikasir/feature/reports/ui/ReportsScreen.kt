package id.stargan.intikasir.feature.reports.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.reports.ui.components.DashboardContent
import id.stargan.intikasir.feature.reports.ui.components.ProfitLossContent
import id.stargan.intikasir.feature.reports.ui.components.ReportsTabRow
import id.stargan.intikasir.feature.reports.ui.components.ReportsTopBar
import id.stargan.intikasir.feature.reports.ui.components.WorstProductsContent
import id.stargan.intikasir.feature.reports.ui.dialogs.ExportDialog
import id.stargan.intikasir.feature.reports.ui.dialogs.PeriodPickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import id.stargan.intikasir.feature.history.util.ExportUtil
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
            ReportsTopBar(
                selectedPeriod = uiState.selectedPeriod,
                onNavigateBack = onNavigateBack,
                onPeriodClick = { viewModel.onEvent(ReportsEvent.ShowPeriodPicker) },
                onRefreshClick = { viewModel.onEvent(ReportsEvent.Refresh) },
                onExportClick = { viewModel.onEvent(ReportsEvent.ShowExportDialog) }
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
            ReportsTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onEvent(ReportsEvent.SelectTab(it)) }
            )

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

                            // Button to load worst selling products for the selected period
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.onEvent(ReportsEvent.LoadWorstProducts()) }) {
                                    Text(text = "Lihat Worst Products")
                                }
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        runCatching {
                                            val file = viewModel.exportWorstProducts(context)
                                            ExportUtil.shareCSV(context, file)
                                        }.onFailure { t ->
                                            // prefer to surface IO/security issues specifically
                                            val msg = when (t) {
                                                is IOException -> "Gagal export worst products: ${t.message}"
                                                else -> "Gagal export worst products"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                }) {
                                    Text(text = "Download Worst (CSV)")
                                }
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        runCatching {
                                            val file = viewModel.exportWorstProductsXlsx(context)
                                            ExportUtil.shareXlsx(context, file)
                                        }.onFailure { t ->
                                            val msg = when (t) {
                                                is IOException -> "Gagal export worst products XLSX: ${t.message}"
                                                else -> "Gagal export worst products XLSX"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                }) {
                                    Text(text = "Download Worst (XLSX)")
                                }
                            }

                            // Display worst products if available
                            uiState.worstProductsReport?.let { report ->
                                WorstProductsContent(report = report)
                            }
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
                    runCatching {
                        val file = viewModel.exportDashboardSummary(context)
                        ExportUtil.shareCSV(context, file)
                        viewModel.onEvent(ReportsEvent.HideExportDialog)
                    }.onFailure { t ->
                        val msg = when (t) {
                            is IOException -> "Gagal export: ${t.message}"
                            else -> "Gagal export"
                        }
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            },
            onExportXlsx = {
                scope.launch {
                    runCatching {
                        val file = viewModel.exportDashboardSummaryXlsx(context)
                        ExportUtil.shareXlsx(context, file)
                        viewModel.onEvent(ReportsEvent.HideExportDialog)
                    }.onFailure { t ->
                        val msg = when (t) {
                            is IOException -> "Gagal export XLSX: ${t.message}"
                            else -> "Gagal export XLSX"
                        }
                        snackbarHostState.showSnackbar(msg)
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
