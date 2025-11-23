package id.stargan.intikasir.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearSuccess()
            // Clear focus after successful save
            focusManager.clearFocus(force = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Pengguna") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Info Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Informasi Akun", style = MaterialTheme.typography.titleMedium)

                    // Username read-only (from current user)
                    OutlinedTextField(
                        value = uiState.username.ifBlank { "(username tidak tersedia)" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nama") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.saveProfile() },
                        enabled = uiState.name.isNotBlank() && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Simpan Nama")
                    }
                }
            }

            // PIN Security Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Keamanan PIN", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = uiState.newPin,
                        onValueChange = viewModel::onNewPinChange,
                        label = { Text("PIN Baru") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        supportingText = {
                            if (uiState.newPin.isNotBlank()) {
                                Text(text = "PIN harus 4-6 digit angka")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.confirmPin,
                        onValueChange = viewModel::onConfirmPinChange,
                        label = { Text("Konfirmasi PIN") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = uiState.newPin.isNotBlank(),
                        isError = uiState.confirmPin.isNotBlank() && uiState.confirmPin != uiState.newPin,
                        supportingText = {
                            if (uiState.confirmPin.isNotBlank() && uiState.confirmPin != uiState.newPin) {
                                Text(text = "PIN tidak cocok", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.newPin.isNotBlank()) {
                        OutlinedTextField(
                            value = uiState.oldPin,
                            onValueChange = viewModel::onOldPinChange,
                            label = { Text("PIN Lama") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = uiState.oldPin.isBlank() && uiState.newPin.isNotBlank(),
                            supportingText = {
                                if (uiState.oldPin.isBlank()) Text("Masukkan PIN lama untuk verifikasi")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = { viewModel.saveProfile() },
                        enabled = uiState.canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Simpan PIN")
                    }
                }
            }
        }
    }
}
