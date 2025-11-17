package id.stargan.intikasir.feature.reports.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.feature.reports.domain.model.PeriodType
import id.stargan.intikasir.feature.reports.ui.utils.getPeriodLabel

@Composable
fun PeriodPickerDialog(
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

