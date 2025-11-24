package id.stargan.intikasir.feature.security.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // This screen only allows editing permissions for the cashier role.
    // Admin/Owner implicitly has full access and is not configurable here.
    fun roleLabel(roleId: String) = when (roleId) {
        "ADMIN" -> "Owner"
        "CASHIER" -> "Kasir"
        else -> roleId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keamanan & Izin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { /* future: global help or reset */ }) {
                        Icon(Icons.Filled.Security, contentDescription = "Keamanan")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Atur hak akses kasir", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))

            uiState.permission?.let { p ->
                // Grouped by feature
                // Kasir
                SectionHeader(title = "Fitur Kasir")
                PermissionToggleRow("Tambah Transaksi (kasir)", p.canCreateTransaction) { updated ->
                    viewModel.togglePermission(p.copy(canCreateTransaction = updated))
                }

                HorizontalDivider()

                // Produk
                SectionHeader(title = "Fitur Produk")
                PermissionToggleRow("Tambah Produk", p.canCreateProduct) { updated ->
                    viewModel.togglePermission(p.copy(canCreateProduct = updated))
                }
                PermissionToggleRow("Edit Produk", p.canEditProduct) { updated ->
                    viewModel.togglePermission(p.copy(canEditProduct = updated))
                }
                PermissionToggleRow("Hapus Produk", p.canDeleteProduct) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteProduct = updated))
                }

                HorizontalDivider()

                // Kategori
                SectionHeader(title = "Fitur Kategori")
                PermissionToggleRow("Tambah Kategori", p.canCreateCategory) { updated ->
                    viewModel.togglePermission(p.copy(canCreateCategory = updated))
                }
                PermissionToggleRow("Edit Kategori", p.canEditCategory) { updated ->
                    viewModel.togglePermission(p.copy(canEditCategory = updated))
                }
                PermissionToggleRow("Hapus Kategori", p.canDeleteCategory) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteCategory = updated))
                }

                HorizontalDivider()

                // Riwayat
                SectionHeader(title = "Fitur Riwayat")
                PermissionToggleRow("Hapus Transaksi", p.canDeleteTransaction) { updated ->
                    viewModel.togglePermission(p.copy(canDeleteTransaction = updated))
                }

                HorizontalDivider()

                // Pengeluaran
                SectionHeader(title = "Fitur Pengeluaran")
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

                HorizontalDivider()

                // Laporan
                SectionHeader(title = "Fitur Laporan")
                PermissionToggleRow("Lihat Laporan", p.canViewReports) { updated ->
                    viewModel.togglePermission(p.copy(canViewReports = updated))
                }

                HorizontalDivider()

                // Pengaturan
                SectionHeader(title = "Fitur Pengaturan")
                PermissionToggleRow("Edit Pengaturan", p.canEditSettings) { updated ->
                    viewModel.togglePermission(p.copy(canEditSettings = updated))
                }

            } ?: run {
                Text("Memuat data permission...")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Small hint
            Text("Perubahan disimpan otomatis.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun PermissionToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = value, onCheckedChange = onChange)
    }
}
