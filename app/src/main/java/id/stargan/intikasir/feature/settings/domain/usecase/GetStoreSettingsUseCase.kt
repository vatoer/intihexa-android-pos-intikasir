package id.stargan.intikasir.feature.settings.domain.usecase

import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoreSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<StoreSettings?> = repository.getStoreSettings()
}

