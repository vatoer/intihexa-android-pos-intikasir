package id.stargan.intikasir.feature.reports.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.utils.getPeriodLabel
import id.stargan.intikasir.feature.history.ui.components.DateRangePickerModal
import id.stargan.intikasir.feature.history.ui.components.formatDateRange

@Composable
fun ReportsFilterBar(
    selectedPeriod: PeriodType,
    startDate: Long,
    endDate: Long,
    onPeriodChange: (PeriodType) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Periode", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodType.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = {
                        if (period == PeriodType.CUSTOM) {
                            // open date picker
                            showDatePicker = true
                        } else {
                            onPeriodChange(period)
                        }
                    },
                    label = { Text(getPeriodLabel(period)) }
                )
            }
        }

        if (selectedPeriod == PeriodType.CUSTOM) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(formatDateRange(startDate, endDate))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onApply) { Text("Terapkan") }
        }
    }

    DateRangePickerModal(
        showDialog = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateRangeSelected = { s, e ->
            onCustomRangeSelected(s, e)
            showDatePicker = false
        }
    )
}

