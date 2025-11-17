package id.stargan.intikasir.feature.reports.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.feature.reports.ui.components.DashboardContent
import id.stargan.intikasir.feature.reports.ui.components.ProfitLossContent
import id.stargan.intikasir.feature.reports.ui.components.ReportsTabRow
import id.stargan.intikasir.feature.reports.ui.components.ReportsTopBar
import id.stargan.intikasir.feature.reports.ui.dialogs.ExportDialog
import id.stargan.intikasir.feature.reports.ui.dialogs.PeriodPickerDialog
import kotlinx.coroutines.launch

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

