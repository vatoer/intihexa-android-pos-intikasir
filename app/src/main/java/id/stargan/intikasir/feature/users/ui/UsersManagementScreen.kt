package id.stargan.intikasir.feature.users.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsersManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    val filteredUsers = remember(uiState.users, uiState.searchQuery, uiState.filterActive) {
        uiState.users.filter { user ->
            val q = uiState.searchQuery.trim().lowercase()
            val matchSearch = q.isEmpty() || user.name.lowercase().contains(q)
            val matchFilter = when (uiState.filterActive) {
                null -> true
                true -> user.isActive
                false -> !user.isActive
            }
            matchSearch && matchFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Pengguna") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah User")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Cari pengguna...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterActive == null,
                    onClick = { viewModel.setFilterActive(null) },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = uiState.filterActive == true,
                    onClick = { viewModel.setFilterActive(true) },
                    label = { Text("Aktif") }
                )
                FilterChip(
                    selected = uiState.filterActive == false,
                    onClick = { viewModel.setFilterActive(false) },
                    label = { Text("Nonaktif") }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada pengguna untuk filter saat ini")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                user.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            AssistChip(
                                                onClick = {},
                                                enabled = false,
                                                label = { Text(if (user.isActive) "Aktif" else "Nonaktif") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                                    labelColor = if (user.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            )
                                        }
                                        Text(user.role.displayName(), style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = "Dibuat: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(user.createdAt))}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { viewModel.requestToggleActive(user) }) {
                                            Icon(
                                                imageVector = Icons.Default.ToggleOn,
                                                contentDescription = "Ubah Status",
                                                tint = if (user.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        IconButton(onClick = { viewModel.requestResetPin(user) }) {
                                            Icon(Icons.Default.LockReset, contentDescription = "Reset PIN")
                                        }
                                        IconButton(onClick = { viewModel.requestDeleteUser(user) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus User")
                                        }
                                        IconButton(onClick = { viewModel.startEditUser(user) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit User")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (uiState.showAddDialog) {
        AddUserDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { username, name, role, pin -> viewModel.addUser(username, name, role, pin) }
        )
    }

    // Edit User Dialog
    if (uiState.showEditDialog && uiState.editingUser != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEditDialog() },
            title = { Text("Edit Pengguna") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.editUsername,
                        onValueChange = viewModel::onEditUsernameChange,
                        label = { Text("Username") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.editName,
                        onValueChange = viewModel::onEditNameChange,
                        label = { Text("Nama") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmEditUser() }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissEditDialog() }) { Text("Batal") }
            }
        )
    }

    // Confirm Reset PIN Dialog
    if (uiState.showConfirmResetPin && uiState.pendingActionUser != null) {
        val user = uiState.pendingActionUser
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Reset PIN") },
            text = { Text("Reset PIN pengguna '${user!!.name}' ke default?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmResetPin) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Batal") }
            }
        )
    }

    // Confirm Delete Dialog
    if (uiState.showConfirmDelete && uiState.pendingActionUser != null) {
        val user = uiState.pendingActionUser
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Hapus Pengguna") },
            text = { Text("Hapus pengguna '${user!!.name}'? Tindakan ini bersifat soft delete.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteUser) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Batal") }
            }
        )
    }

    // Confirm Toggle Active Dialog
    if (uiState.showConfirmToggleActive && uiState.pendingActionUser != null) {
        val target = uiState.pendingActionUser
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Ubah Status") },
            text = { Text("Ubah status pengguna '${target!!.name}' menjadi ${if (target.isActive) "Nonaktif" else "Aktif"}?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmToggleActive) { Text("Ubah") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun RoleSelector(selectedRole: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            selected = selectedRole == "ADMIN",
            onClick = { onSelect("ADMIN") },
            label = { Text("Admin") },
            leadingIcon = if (selectedRole == "ADMIN") { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = if (selectedRole == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (selectedRole == "ADMIN") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = selectedRole == "CASHIER",
            onClick = { onSelect("CASHIER") },
            label = { Text("Kasir") },
            leadingIcon = if (selectedRole == "CASHIER") { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = if (selectedRole == "CASHIER") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (selectedRole == "CASHIER") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CASHIER") }
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pengguna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim() },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { ch -> ch.isDigit() } },
                    label = { Text("PIN (4-6 digit)") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Pilih Peran", style = MaterialTheme.typography.labelMedium)
                RoleSelector(selectedRole = role, onSelect = { role = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(username, name, role, pin) },
                enabled = username.isNotBlank() && name.isNotBlank() && pin.length in 4..6
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
