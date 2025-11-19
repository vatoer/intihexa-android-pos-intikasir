package id.stargan.intikasir.feature.history.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.data.local.entity.TransactionStatus
import id.stargan.intikasir.feature.history.ui.DateRange
import id.stargan.intikasir.feature.history.ui.components.DateRangePickerModal
import id.stargan.intikasir.feature.history.ui.components.formatDateRange

@Composable
fun HistoryFilterBar(
    selectedRange: DateRange,
    startDate: Long,
    endDate: Long,
    selectedStatus: TransactionStatus?,
    onRangeChange: (DateRange) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onStatusChange: (TransactionStatus?) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Periode", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRange.entries.forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeChange(range) },
                    label = { Text(range.label) }
                )
            }
        }
        if (selectedRange == DateRange.CUSTOM) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(formatDateRange(startDate, endDate))
            }
        }
        Text("Status", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusChange(null) },
                label = { Text("Semua") }
            )
            TransactionStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusChange(status) },
                    label = { Text(status.name) }
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onApply) { Text("Terapkan") }
        }
    }

    DateRangePickerModal(
        showDialog = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateRangeSelected = { start, end ->
            onStartDateChange(start)
            onEndDateChange(end)
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "History Filter Bar - Expanded", showBackground = true)
@Composable
private fun HistoryFilterBarPreview() {
    MaterialTheme {
        HistoryFilterBar(
            selectedRange = DateRange.LAST_7_DAYS,
            startDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
            endDate = System.currentTimeMillis(),
            selectedStatus = null,
            onRangeChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onStatusChange = {},
            onApply = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "History Filter Bar - With Status Filter", showBackground = true)
@Composable
private fun HistoryFilterBarWithStatusPreview() {
    MaterialTheme {
        HistoryFilterBar(
            selectedRange = DateRange.TODAY,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis(),
            selectedStatus = TransactionStatus.PAID,
            onRangeChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onStatusChange = {},
            onApply = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "History Filter Bar - Custom Range", showBackground = true)
@Composable
private fun HistoryFilterBarCustomRangePreview() {
    MaterialTheme {
        HistoryFilterBar(
            selectedRange = DateRange.CUSTOM,
            startDate = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
            endDate = System.currentTimeMillis(),
            selectedStatus = TransactionStatus.COMPLETED,
            onRangeChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onStatusChange = {},
            onApply = {}
        )
    }
}

