package id.stargan.intikasir.feature.security.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.data.local.entity.RolePermissionEntity
import id.stargan.intikasir.feature.security.data.RolePermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val repository: RolePermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    // current selected role id in UI (default CASHIER)
    private val _currentRoleId = MutableStateFlow("CASHIER")
    val currentRoleId: StateFlow<String> = _currentRoleId.asStateFlow()

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        viewModelScope.launch {
            // load cashier permissions or create default if not present
            val existing = repository.getPermissionsForRoleSuspend("CASHIER")
            if (existing == null) {
                val default = RolePermissionEntity(
                    roleId = "CASHIER",
                    canCreateTransaction = true,
                    canCreateProduct = false,
                    canEditProduct = false,
                    canDeleteProduct = false,
                    canCreateCategory = false,
                    canEditCategory = false,
                    canDeleteCategory = false,
                    canDeleteTransaction = false,
                    canViewExpense = false,
                    canCreateExpense = false,
                    canEditExpense = false,
                    canDeleteExpense = false,
                    canViewReports = false,
                    canEditSettings = false
                )
                repository.upsert(default)
                _uiState.update { it.copy(permission = default) }
            } else {
                _uiState.update { it.copy(permission = existing) }
            }
        }
    }

    fun togglePermission(update: RolePermissionEntity) {
        viewModelScope.launch {
            repository.upsert(update.copy(updatedAt = System.currentTimeMillis()))
            _uiState.update { it.copy(permission = update) }
        }
    }

    // Helper: observe a boolean permission derived from RolePermissionEntity for a given roleId
    fun observePermission(roleId: String, selector: (RolePermissionEntity) -> Boolean) =
        repository.getPermissionsForRole(roleId).map { it?.let(selector) ?: false }

    // Helper: suspend check permission once
    suspend fun hasPermissionSuspend(roleId: String, selector: (RolePermissionEntity) -> Boolean): Boolean {
        val p = repository.getPermissionsForRoleSuspend(roleId)
        return p?.let(selector) ?: false
    }

    fun setRole(roleId: String) {
        if (roleId == _currentRoleId.value) return
        _currentRoleId.value = roleId
        loadRolePermissions(roleId)
    }

    private fun loadRolePermissions(roleId: String) {
        viewModelScope.launch {
            val existing = repository.getPermissionsForRoleSuspend(roleId)
            if (existing == null) {
                // Create a safe default (restrictive)
                val default = RolePermissionEntity(roleId = roleId)
                repository.upsert(default)
                _uiState.update { it.copy(permission = default) }
            } else {
                _uiState.update { it.copy(permission = existing) }
            }
        }
    }
}

data class SecurityUiState(
    val permission: RolePermissionEntity? = null
)
