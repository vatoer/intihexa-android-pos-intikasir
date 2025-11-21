package id.stargan.intikasir.feature.activation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.stargan.intikasir.feature.activation.ActivationState
import id.stargan.intikasir.feature.activation.ActivationViewModel

@Composable
fun ActivationInfoCard(
    modifier: Modifier = Modifier,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val isActivated by viewModel.isActivated.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()
    val serialNumber by viewModel.serialNumber.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val activationState by viewModel.activationState.collectAsState()

    var showActivationDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActivated)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isActivated) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isActivated)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Status Aktivasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            if (isActivated) {
                InfoRow("Status", "Aktif")
                InfoRow("Serial Number", serialNumber)
                InfoRow("Berlaku Hingga", expiryDate)
            } else {
                InfoRow("Status", "Belum Diaktivasi")
                InfoRow("Device ID", deviceId)

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showActivationDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aktivasi Sekarang")
                }
            }
        }
    }

    // Activation Dialog
    if (showActivationDialog) {
        ActivationDialog(
            onDismiss = { showActivationDialog = false },
            onActivate = { sn ->
                viewModel.activate(sn)
            },
            activationState = activationState,
            deviceId = deviceId
        )
    }

    // Auto close dialog on success
    LaunchedEffect(activationState) {
        if (activationState is ActivationState.Success) {
            kotlinx.coroutines.delay(2000)
            showActivationDialog = false
            viewModel.resetState()
        }
    }
}

@Composable
private fun ActivationDialog(
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit,
    activationState: ActivationState,
    deviceId: String
) {
    var serialNumberInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (activationState !is ActivationState.Loading) {
                onDismiss()
            }
        },
        icon = {
            Icon(Icons.Default.Key, contentDescription = null)
        },
        title = {
            Text("Aktivasi Aplikasi")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Device ID Anda:")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = deviceId,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                OutlinedTextField(
                    value = serialNumberInput,
                    onValueChange = { serialNumberInput = it.uppercase() },
                    label = { Text("Serial Number") },
                    placeholder = { Text("Masukkan Serial Number") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = activationState !is ActivationState.Loading,
                    singleLine = true
                )

                when (val state = activationState) {
                    is ActivationState.Success -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is ActivationState.Error -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onActivate(serialNumberInput) },
                enabled = activationState !is ActivationState.Loading && serialNumberInput.isNotBlank()
            ) {
                if (activationState is ActivationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (activationState is ActivationState.Loading) "Mengaktivasi..." else "Aktivasi")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = activationState !is ActivationState.Loading
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

