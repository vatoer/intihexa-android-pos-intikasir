package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.utils.getPeriodLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsTopBar(
    selectedPeriod: PeriodType,
    onNavigateBack: () -> Unit,
    onPeriodClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onExportClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Laporan") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        },
        actions = {
            // Period selector
            TextButton(onClick = onPeriodClick) {
                Text(getPeriodLabel(selectedPeriod))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            // Refresh
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }

            // Export
            IconButton(onClick = onExportClick) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

