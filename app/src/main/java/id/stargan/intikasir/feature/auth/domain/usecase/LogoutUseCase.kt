package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case untuk logout
 * Single responsibility: handle logout logic
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute logout
     * Clears session and user data
     */
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

