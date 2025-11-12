package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk check authentication status
 * Single responsibility: check if user is logged in
 */
class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Check if user is logged in
     * @return Flow<Boolean> true if logged in, false otherwise
     */
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}

