package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.domain.model.User
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk get current logged in user
 * Single responsibility: retrieve current user
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Get current user
     * @return Flow<User?> current logged in user or null
     */
    operator fun invoke(): Flow<User?> {
        return authRepository.getCurrentUser()
    }
}

