package id.stargan.intikasir.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.usecase.GetCurrentUserUseCase
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.security.MessageDigest

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val userId: String? = null,
        val name: String = "",
        val newPin: String = "",
        val confirmPin: String = "",
        val oldPin: String = "",
        val isSaving: Boolean = false,
        val successMessage: String? = null,
        val errorMessage: String? = null,
        val currentHashedPin: String = "",
        val username: String = ""
    ) {
        val canSave: Boolean get() = name.isNotBlank() &&
            (newPin.isBlank() || (newPin.length in 4..6 && newPin == confirmPin && oldPin.isNotBlank())) && !isSaving
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        userId = user?.id,
                        name = user?.name ?: "",
                        currentHashedPin = user?.pin ?: "",
                        username = user?.username ?: ""
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onNewPinChange(value: String) {
        _uiState.update { it.copy(newPin = value.filter { ch -> ch.isDigit() }) }
    }

    fun onConfirmPinChange(value: String) {
        _uiState.update { it.copy(confirmPin = value.filter { ch -> ch.isDigit() }) }
    }

    fun onOldPinChange(value: String) {
        _uiState.update { it.copy(oldPin = value.filter { ch -> ch.isDigit() }) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val userId = state.userId ?: run {
            _uiState.update { it.copy(errorMessage = "User tidak ditemukan") }
            return
        }
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                if (state.name.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Nama tidak boleh kosong", isSaving = false) }
                    return@launch
                }
                var hashedPin: String? = null
                if (state.newPin.isNotBlank()) {
                    if (state.oldPin.isBlank()) {
                        _uiState.update { it.copy(errorMessage = "Masukkan PIN lama", isSaving = false) }
                        return@launch
                    }
                    val validOld = authRepository.verifyPin(state.oldPin, state.currentHashedPin)
                    if (!validOld) {
                        _uiState.update { it.copy(errorMessage = "PIN lama tidak sesuai", isSaving = false) }
                        return@launch
                    }
                    val formatCheck = authRepository.validatePinFormat(state.newPin)
                    if (formatCheck.isFailure) {
                        _uiState.update { it.copy(errorMessage = formatCheck.exceptionOrNull()?.message ?: "Format PIN baru tidak valid", isSaving = false) }
                        return@launch
                    }
                    if (state.newPin != state.confirmPin) {
                        _uiState.update { it.copy(errorMessage = "Konfirmasi PIN tidak cocok", isSaving = false) }
                        return@launch
                    }
                    hashedPin = authRepository.hashPin(state.newPin)
                }
                authRepository.updateUserProfile(
                    userId = userId,
                    newName = state.name.trim(),
                    newHashedPin = hashedPin
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Profil berhasil diperbarui",
                        newPin = "",
                        confirmPin = "",
                        oldPin = "",
                        currentHashedPin = hashedPin ?: it.currentHashedPin
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Gagal menyimpan profil") }
            }
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
}
