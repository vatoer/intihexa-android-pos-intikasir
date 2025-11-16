package id.stargan.intikasir.feature.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.stargan.intikasir.feature.auth.domain.model.AuthErrorType
import id.stargan.intikasir.feature.auth.domain.model.AuthResult
import id.stargan.intikasir.feature.auth.domain.usecase.LoginUseCase
import id.stargan.intikasir.feature.auth.domain.usecase.ValidatePinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk Login Screen
 * Menggunakan MVI pattern dengan single state flow
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validatePinUseCase: ValidatePinUseCase,
    private val authRepository: id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Handle UI events
     */
    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.UsernameChanged -> _uiState.update { it.copy(username = event.username.trim(), usernameError = null) }
            LoginUiEvent.NextFromUsername -> handleNextFromUsername()
            LoginUiEvent.BackToUsername -> _uiState.update { it.copy(step = LoginStep.USERNAME, pin = "", showPinError = false, pinErrorMessage = null) }
            is LoginUiEvent.PinChanged -> handlePinChanged(event.pin)
            LoginUiEvent.LoginClicked -> handleLoginWithSelectedUser()
            LoginUiEvent.ClearPin -> clearPin()
            LoginUiEvent.DismissError -> dismissError()
            LoginUiEvent.NavigatedToHome -> _uiState.update { it.copy(isSuccess = false) }
        }
    }

    /**
     * Handle PIN input change
     */
    private fun handlePinChanged(pin: String) {
        // Only allow numeric input and max 6 digits
        val filteredPin = pin.filter { it.isDigit() }.take(6)

        _uiState.update { currentState ->
            currentState.copy(
                pin = filteredPin,
                showPinError = false,
                pinErrorMessage = null,
                error = null
            )
        }

        // Note: Tidak perlu validasi real-time saat user mengetik
        // Validasi hanya dilakukan saat user klik tombol Login
        // Ini memberikan UX yang lebih baik karena user tidak diganggu
        // dengan error message saat masih mengetik PIN
    }

    /**
     * Handle login action
     */
    private fun handleLoginWithSelectedUser() {
        val state = _uiState.value
        val userId = state.selectedUserId
        if (state.step != LoginStep.PIN || userId == null) {
            _uiState.update { it.copy(error = "Silakan pilih username terlebih dahulu") }
            return
        }
        val currentPin = state.pin
        // Validate PIN before login
        val validation = validatePinUseCase(currentPin)
        if (validation.isFailure) {
            _uiState.update { it.copy(showPinError = true, pinErrorMessage = validation.exceptionOrNull()?.message) }
            return
        }

        // Proceed with login
        viewModelScope.launch {
            authRepository.loginWithUser(userId, currentPin).collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update { it.copy(isLoading = true, error = null, errorType = null) }
                    is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true, loggedInUser = result.user, error = null, errorType = null, pin = "") }
                    is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message, errorType = mapAuthErrorToUiError(result.errorType), isSuccess = false) }
                    is AuthResult.Idle -> {}
                }
            }
        }
    }

    private fun handleNextFromUsername() {
        val uname = _uiState.value.username
        if (uname.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username tidak boleh kosong") }
            return
        }
        viewModelScope.launch {
            val user = authRepository.findUserByUsername(uname)
            if (user == null) {
                _uiState.update { it.copy(usernameError = "Username tidak ditemukan") }
                return@launch
            }
            if (!user.isActive) {
                _uiState.update { it.copy(usernameError = "Akun nonaktif. Hubungi admin.") }
                return@launch
            }
            _uiState.update { it.copy(step = LoginStep.PIN, selectedUserId = user.id, selectedUserName = user.name, error = null, errorType = null, pin = "") }
        }
    }

    /**
     * Clear PIN input
     */
    private fun clearPin() {
        _uiState.update { currentState ->
            currentState.copy(
                pin = "",
                showPinError = false,
                pinErrorMessage = null
            )
        }
    }

    /**
     * Dismiss error message
     */
    private fun dismissError() {
        _uiState.update { currentState ->
            currentState.copy(
                error = null,
                errorType = null
            )
        }
    }

    /**
     * Map domain error type to UI error type
     */
    private fun mapAuthErrorToUiError(errorType: AuthErrorType): LoginErrorType {
        return when (errorType) {
            AuthErrorType.INVALID_PIN -> LoginErrorType.INVALID_PIN
            AuthErrorType.USER_NOT_FOUND -> LoginErrorType.USER_NOT_FOUND
            AuthErrorType.USER_INACTIVE -> LoginErrorType.USER_INACTIVE
            AuthErrorType.NETWORK_ERROR -> LoginErrorType.NETWORK_ERROR
            else -> LoginErrorType.UNKNOWN
        }
    }
}
