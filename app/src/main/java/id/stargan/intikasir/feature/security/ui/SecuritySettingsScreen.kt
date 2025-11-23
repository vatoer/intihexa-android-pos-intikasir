package id.stargan.intikasir.feature.security.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import id.stargan.intikasir.data.local.entity.RolePermissionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentRole by viewModel.currentRoleId.collectAsState()
    val permission = uiState.permission

    // small role list for now
    val roles = listOf("CASHIER", "ADMIN")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keamanan & Izin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Atur hak akses per role", style = MaterialTheme.typography.titleMedium)

            // Role selector
            ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                OutlinedTextField(value = currentRole, onValueChange = {}, readOnly = true, label = { Text("Role") })
                // Quick row to choose role
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { role ->
                        AssistChip(onClick = { viewModel.setRole(role) }, label = { Text(role) }, leadingIcon = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            permission?.let { p ->
                // Full set of toggles mapped from RolePermissionEntity
                PermissionToggleRow("Tambah Transaksi (Kasir)", p.canCreateTransaction) { updated ->
                    viewModel.togglePermission(p.copy(canCreateTransaction = updated))
                }
                PermissionToggleRow("Tambah Produk", p.canCreateProduct) { updated ->
                    viewModel.togglePermission(p.copy(canCreateProduct = updated))
                }
                PermissionToggleRow("Edit Produk", p.canEditProduct) { updated ->
                    viewModel.togglePermission(p.copy(canEditProduct = updated))
                }
                PermissionToggleRow("Hapus Produk", p.canDeleteProduct) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteProduct = updated))
                }
                PermissionToggleRow("Tambah Kategori", p.canCreateCategory) { updated ->
                    viewModel.togglePermission(p.copy(canCreateCategory = updated))
                }
                PermissionToggleRow("Edit Kategori", p.canEditCategory) { updated ->
                    viewModel.togglePermission(p.copy(canEditCategory = updated))
                }
                PermissionToggleRow("Hapus Kategori", p.canDeleteCategory) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteCategory = updated))
                }
                PermissionToggleRow("Hapus Transaksi (Riwayat)", p.canDeleteTransaction) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteTransaction = updated))
                }
                PermissionToggleRow("Lihat Pengeluaran", p.canViewExpense) { updated ->
                    viewModel.togglePermission(p.copy(canViewExpense = updated))
                }
                PermissionToggleRow("Tambah Pengeluaran", p.canCreateExpense) { updated ->
                    viewModel.togglePermission(p.copy(canCreateExpense = updated))
                }
                PermissionToggleRow("Edit Pengeluaran", p.canEditExpense) { updated ->
                    viewModel.togglePermission(p.copy(canEditExpense = updated))
                }
                PermissionToggleRow("Hapus Pengeluaran", p.canDeleteExpense) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteExpense = updated))
                }
                PermissionToggleRow("Lihat Laporan", p.canViewReports) { updated ->
                    viewModel.togglePermission(p.copy(canViewReports = updated))
                }
                PermissionToggleRow("Edit Pengaturan", p.canEditSettings) { updated ->
                    viewModel.togglePermission(p.copy(canEditSettings = updated))
                }
            } ?: run {
                Text("Memuat data permission...")
            }
        }
    }
}

@Composable
private fun PermissionToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label)
        Switch(checked = value, onCheckedChange = onChange)
    }
}
