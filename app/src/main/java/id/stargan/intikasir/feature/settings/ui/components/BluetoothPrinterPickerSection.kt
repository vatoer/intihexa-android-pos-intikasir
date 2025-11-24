package id.stargan.intikasir.feature.settings.ui.components

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.stargan.intikasir.domain.model.StoreSettings

@Composable
fun BluetoothPrinterPickerSection(
    modifier: Modifier = Modifier,
    adapter: BluetoothAdapter?,
    hasBtPermissions: Boolean,
    bluetoothPermissions: Array<String>,
    onRequestPermissions: (Array<String>) -> Unit,
    settings: StoreSettings?,
    onSavePrinter: (name: String, address: String) -> Unit,
    isEditable: Boolean = true,
) {
    var bonded by remember { mutableStateOf<List<android.bluetooth.BluetoothDevice>>(emptyList()) }
    var selectedAddress by remember(settings) { mutableStateOf(settings?.printerAddress) }

    LaunchedEffect(adapter, hasBtPermissions) {
        bonded = if (adapter != null && hasBtPermissions) {
            try {
                adapter.bondedDevices?.toList() ?: emptyList()
            } catch (_: SecurityException) {
                emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pilih Printer Bluetooth", style = MaterialTheme.typography.titleMedium)
            val activeName = settings?.printerName
            val activeAddr = settings?.printerAddress
            if (!activeAddr.isNullOrBlank()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Printer aktif:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeName ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                        Text(activeAddr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (!isEditable) {
                if (!activeAddr.isNullOrBlank()) {
                    Text("Pengaturan printer dikunci (tidak ada izin)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Printer tidak diatur dan Anda tidak memiliki izin untuk mengubahnya.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                if (adapter == null) {
                    Text("Bluetooth tidak tersedia di perangkat ini", color = MaterialTheme.colorScheme.error)
                } else if (!adapter.isEnabled) {
                    Text("Bluetooth nonaktif. Aktifkan Bluetooth untuk memilih printer.", color = MaterialTheme.colorScheme.error)
                } else if (!hasBtPermissions && bluetoothPermissions.isNotEmpty()) {
                    Text("Izin BLUETOOTH_CONNECT/SCAN diperlukan untuk melihat perangkat.")
                    OutlinedButton(onClick = { onRequestPermissions(bluetoothPermissions) }) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Izinkan Bluetooth")
                    }
                } else if (bonded.isEmpty()) {
                    Text("Tidak ada perangkat Bluetooth yang terpasang.")
                    Text("Silakan pasangkan printer terlebih dahulu melalui pengaturan Bluetooth.", style = MaterialTheme.typography.bodySmall)
                } else {
                    bonded.forEach { device ->
                        val name = try { device.name } catch (_: SecurityException) { null } ?: "Unknown"
                        val addr = try { device.address } catch (_: SecurityException) { null }
                        if (addr != null) {
                            val selected = selectedAddress == addr
                            OutlinedButton(
                                onClick = {
                                    selectedAddress = addr
                                    onSavePrinter(name, addr)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(name)
                                        Text(addr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (selected) Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
