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
    val pinErrorMessage: String? = null,
    val step: LoginStep = LoginStep.USERNAME,
    val username: String = "",
    val usernameError: String? = null,
    val selectedUserId: String? = null,
    val selectedUserName: String? = null
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

enum class LoginStep { USERNAME, PIN }

/**
 * UI Events untuk Login Screen
 */
sealed class LoginUiEvent {
    data class PinChanged(val pin: String) : LoginUiEvent()
    data object LoginClicked : LoginUiEvent()
    data object ClearPin : LoginUiEvent()
    data object DismissError : LoginUiEvent()
    data object NavigatedToHome : LoginUiEvent()
    data class UsernameChanged(val username: String) : LoginUiEvent()
    data object NextFromUsername : LoginUiEvent()
    data object BackToUsername : LoginUiEvent()
}
