package id.stargan.intikasir.feature.users.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.domain.model.User
import id.stargan.intikasir.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersManagementViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    data class UiState(
        val users: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val showAddDialog: Boolean = false,
        val isAdmin: Boolean = false,
        val searchQuery: String = "",
        val filterActive: Boolean? = null, // null = all, true = active only, false = inactive only
        val pendingActionUser: User? = null,
        val showConfirmResetPin: Boolean = false,
        val showConfirmDelete: Boolean = false,
        val showConfirmToggleActive: Boolean = false,
        val showEditDialog: Boolean = false,
        val editingUser: User? = null,
        val editUsername: String = "",
        val editName: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        loadUsers()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update { it.copy(isAdmin = user?.role == UserRole.ADMIN) }
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            authRepository.getAllUsers().collect { list ->
                _uiState.update { it.copy(users = list.sortedBy { u -> u.name }, isLoading = false) }
            }
        }
    }

    fun toggleActive(user: User) {
        // Prevent admin deactivating themselves
        val currentAdminSelf = _uiState.value.isAdmin && user.role == UserRole.ADMIN && user.isActive && user.id == _uiState.value.users.find { it.role == UserRole.ADMIN && it.id == user.id }?.id
        if (currentAdminSelf && user.isActive) {
            _uiState.update { it.copy(errorMessage = "Tidak dapat menonaktifkan akun admin yang sedang digunakan") }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.toggleUserActive(user.id, !user.isActive)
                _uiState.update { it.copy(successMessage = "Status pengguna diperbarui") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal memperbarui status") }
            }
        }
    }

    fun resetPin(user: User) {
        viewModelScope.launch {
            try {
                val defaultPin = if (user.role == UserRole.ADMIN) "1111" else "2222"
                val hashed = authRepository.hashPin(defaultPin)
                authRepository.resetUserPin(user.id, hashed)
                _uiState.update { it.copy(successMessage = "PIN direset ke $defaultPin") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal reset PIN") }
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                authRepository.softDeleteUser(user.id)
                _uiState.update { it.copy(successMessage = "Pengguna dihapus") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal menghapus user") }
            }
        }
    }

    fun addUser(username: String, name: String, role: String, pin: String) {
        viewModelScope.launch {
            try {
                val uname = username.trim()
                val trimmed = name.trim()
                val existsName = _uiState.value.users.any { it.name.equals(trimmed, ignoreCase = true) }
                val existsUser = _uiState.value.users.any { it.username.equals(uname, ignoreCase = true) }
                if (existsUser) {
                    _uiState.update { it.copy(errorMessage = "Username sudah digunakan") }
                    return@launch
                }
                if (existsName) {
                    _uiState.update { it.copy(errorMessage = "Nama pengguna sudah ada") }
                    return@launch
                }
                val hashed = authRepository.hashPin(pin)
                val domainRole = UserRole.valueOf(role)
                authRepository.createUser(uname, trimmed, domainRole, hashed)
                _uiState.update { it.copy(successMessage = "User $trimmed ditambahkan") }
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal menambah user") }
            }
        }
    }

    fun showAddDialog() { _uiState.update { it.copy(showAddDialog = true) } }
    fun hideAddDialog() { _uiState.update { it.copy(showAddDialog = false) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
    fun onSearchChange(q: String) { _uiState.update { it.copy(searchQuery = q) } }
    fun setFilterActive(value: Boolean?) { _uiState.update { it.copy(filterActive = value) } }

    fun requestResetPin(user: User) {
        _uiState.update { it.copy(pendingActionUser = user, showConfirmResetPin = true) }
    }
    fun requestDeleteUser(user: User) {
        _uiState.update { it.copy(pendingActionUser = user, showConfirmDelete = true) }
    }
    fun requestToggleActive(user: User) {
        _uiState.update { it.copy(pendingActionUser = user, showConfirmToggleActive = true) }
    }
    private fun clearPendingDialogs() {
        _uiState.update { it.copy(pendingActionUser = null, showConfirmResetPin = false, showConfirmDelete = false, showConfirmToggleActive = false) }
    }

    fun confirmResetPin() {
        val user = _uiState.value.pendingActionUser ?: return
        resetPin(user)
        clearPendingDialogs()
    }
    fun confirmDeleteUser() {
        val user = _uiState.value.pendingActionUser ?: return
        deleteUser(user)
        clearPendingDialogs()
    }
    fun confirmToggleActive() {
        val user = _uiState.value.pendingActionUser ?: return
        toggleActive(user)
        clearPendingDialogs()
    }
    fun dismissDialogs() = clearPendingDialogs()

    fun startEditUser(user: User) {
        _uiState.update { it.copy(showEditDialog = true, editingUser = user, editUsername = user.username, editName = user.name) }
    }
    fun onEditUsernameChange(v: String) { _uiState.update { it.copy(editUsername = v.trim()) } }
    fun onEditNameChange(v: String) { _uiState.update { it.copy(editName = v) } }
    fun dismissEditDialog() { _uiState.update { it.copy(showEditDialog = false, editingUser = null) } }

    fun confirmEditUser() {
        val st = _uiState.value
        val target = st.editingUser ?: return
        val uname = st.editUsername.trim()
        val name = st.editName.trim()
        if (uname.isBlank() || name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username dan Nama tidak boleh kosong") }
            return
        }
        // Uniqueness check (exclude current user)
        val existsUser = st.users.any { it.id != target.id && it.username.equals(uname, ignoreCase = true) }
        val existsName = st.users.any { it.id != target.id && it.name.equals(name, ignoreCase = true) }
        if (existsUser) {
            _uiState.update { it.copy(errorMessage = "Username sudah digunakan") }
            return
        }
        if (existsName) {
            _uiState.update { it.copy(errorMessage = "Nama pengguna sudah ada") }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.updateUserAccount(target.id, uname, name)
                _uiState.update { it.copy(successMessage = "Data pengguna diperbarui", showEditDialog = false, editingUser = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Gagal memperbarui pengguna") }
            }
        }
    }
}
