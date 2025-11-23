package id.stargan.intikasir.feature.history.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import id.stargan.intikasir.util.DateFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    if (showDialog) {
        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val startDate = dateRangePickerState.selectedStartDateMillis
                        val endDate = dateRangePickerState.selectedEndDateMillis
                        if (startDate != null && endDate != null) {
                            onDateRangeSelected(startDate, endDate)
                        }
                        onDismiss()
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null &&
                             dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier
            )
        }
    }
}

fun formatDateRange(startDate: Long, endDate: Long): String {
    val pattern = "dd MMM yyyy"
    return "${DateFormatUtils.formatEpochMillis(startDate, pattern)} - ${DateFormatUtils.formatEpochMillis(endDate, pattern)}"
}
