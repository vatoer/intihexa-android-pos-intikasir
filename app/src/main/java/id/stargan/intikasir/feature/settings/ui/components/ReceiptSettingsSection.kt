package id.stargan.intikasir.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.StoreSettings

@Composable
fun ReceiptSettingsSection(
    settings: StoreSettings,
    onSave: (StoreSettings) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }

    var header by remember(settings) { mutableStateOf(settings.receiptHeader ?: "") }
    var footer by remember(settings) { mutableStateOf(settings.receiptFooter ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Pengaturan Struk", style = MaterialTheme.typography.titleMedium)
                if (!editMode) {
                    TextButton(onClick = { editMode = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
            HorizontalDivider()

            if (!editMode) {
                LabelValueRow("Header", header.ifBlank { "-" })
                LabelValueRow("Footer", footer.ifBlank { "-" })
            } else {
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    label = { Text("Header Struk (opsional)") },
                    minLines = 2,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = { Text("Footer Struk (opsional)") },
                    minLines = 2,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        header = settings.receiptHeader ?: ""
                        footer = settings.receiptFooter ?: ""
                        editMode = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val updated = settings.copy(
                            receiptHeader = header.ifBlank { null },
                            receiptFooter = footer.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updated)
                        editMode = false
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

