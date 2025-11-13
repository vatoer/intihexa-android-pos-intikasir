package id.stargan.intikasir.feature.auth.domain.usecase

import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case untuk inisialisasi user default
 * Dipanggil saat aplikasi pertama kali dijalankan
 */
class InitializeDefaultUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute inisialisasi user default
     * Hanya akan membuat user default jika database masih kosong
     */
    suspend operator fun invoke() {
        authRepository.initializeDefaultUsers()
    }
}

