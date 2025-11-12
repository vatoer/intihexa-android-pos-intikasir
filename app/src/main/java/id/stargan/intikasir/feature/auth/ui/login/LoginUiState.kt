package id.stargan.intikasir.feature.auth.ui.login

import id.stargan.intikasir.domain.model.User

/**
 * UI State untuk Login Screen
 */
data class LoginUiState(
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorType: LoginErrorType? = null,
    val isSuccess: Boolean = false,
    val loggedInUser: User? = null,
    val showPinError: Boolean = false,
    val pinErrorMessage: String? = null
)

/**
 * Login error types untuk UI feedback
 */
enum class LoginErrorType {
    INVALID_PIN,
    USER_NOT_FOUND,
    USER_INACTIVE,
    NETWORK_ERROR,
    UNKNOWN
}

/**
 * UI Events untuk Login Screen
 */
sealed class LoginUiEvent {
    data class PinChanged(val pin: String) : LoginUiEvent()
    data object LoginClicked : LoginUiEvent()
    data object ClearPin : LoginUiEvent()
    data object DismissError : LoginUiEvent()
    data object NavigatedToHome : LoginUiEvent()
}

