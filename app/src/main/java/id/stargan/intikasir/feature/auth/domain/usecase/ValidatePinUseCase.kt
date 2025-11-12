package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case untuk validate PIN format
 * Single responsibility: validate PIN rules
 */
class ValidatePinUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Validate PIN format
     * Rules:
     * - Must be numeric only
     * - Must be 4-6 digits
     *
     * @param pin PIN to validate
     * @return Result<Unit> success if valid, error with message if invalid
     */
    operator fun invoke(pin: String): Result<Unit> {
        return authRepository.validatePinFormat(pin)
    }
}

