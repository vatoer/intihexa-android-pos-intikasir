package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.feature.auth.domain.model.AuthResult
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk login
 * Single responsibility: handle login logic
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute login
     * @param pin PIN yang diinput user
     * @return Flow<AuthResult> untuk observe hasil login
     */
    suspend operator fun invoke(pin: String): Flow<AuthResult> {
        // Validate PIN format first
        val validation = authRepository.validatePinFormat(pin)
        if (validation.isFailure) {
            return kotlinx.coroutines.flow.flowOf(
                AuthResult.Error(
                    message = validation.exceptionOrNull()?.message ?: "Invalid PIN format",
                    errorType = id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.PIN_TOO_SHORT
                )
            )
        }

        // Proceed with login
        return authRepository.login(pin)
    }
}

