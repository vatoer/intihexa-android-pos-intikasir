package id.stargan.intikasir.feature.settings.domain.usecase

import id.stargan.intikasir.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateStoreLogoUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(logoPath: String?) {
        repository.updateStoreLogo(logoPath)
    }
}

