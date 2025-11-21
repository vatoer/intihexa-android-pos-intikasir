package id.stargan.intikasir.core.navigation

import id.stargan.intikasir.data.repository.ActivationRepository

class ActivationGuard(
    private val activationRepository: ActivationRepository
) {
    /**
     * Cek apakah device sudah diaktivasi
     * @return true jika sudah aktif dan valid, false jika belum
     */
    fun checkActivation(): Boolean {
        return activationRepository.isActivated()
    }

    /**
     * Get device ID untuk ditampilkan
     */
    fun getDeviceId(): String {
        return activationRepository.getDeviceId()
    }
}

