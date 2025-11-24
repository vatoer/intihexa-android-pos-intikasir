package id.stargan.intikasir.feature.settings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.StoreSettings

@Composable
fun StoreInfoSection(
    settings: StoreSettings,
    onSave: (StoreSettings) -> Unit,
    isEditable: Boolean = true
) {
    var editMode by remember { mutableStateOf(false) }
    // Prevent edit mode when user cannot edit
    LaunchedEffect(isEditable) {
        if (!isEditable) editMode = false
    }

    var name by remember(settings) { mutableStateOf(settings.storeName) }
    var address by remember(settings) { mutableStateOf(settings.storeAddress) }
    var phone by remember(settings) { mutableStateOf(settings.storePhone) }
    var email by remember(settings) { mutableStateOf(settings.storeEmail ?: "") }

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
                Text(text = "Informasi Toko", style = MaterialTheme.typography.titleMedium)
                if (isEditable && !editMode) {
                    TextButton(onClick = { editMode = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
            HorizontalDivider()

            if (!editMode) {
                LabelValueRow("Nama Toko", name.ifBlank { "-" })
                LabelValueRow("Alamat", address.ifBlank { "-" })
                LabelValueRow("Telepon", phone.ifBlank { "-" })
                LabelValueRow("Email", email.ifBlank { "-" })
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Toko") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telepon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (opsional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        // Cancel edit -> reset values to current settings
                        name = settings.storeName
                        address = settings.storeAddress
                        phone = settings.storePhone
                        email = settings.storeEmail ?: ""
                        editMode = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val updated = settings.copy(
                            storeName = name,
                            storeAddress = address,
                            storePhone = phone,
                            storeEmail = email.ifBlank { null },
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
