package id.stargan.intikasir.feature.settings.domain.repository

import id.stargan.intikasir.domain.model.StoreSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getStoreSettings(): Flow<StoreSettings?>
    suspend fun updateStoreSettings(settings: StoreSettings)
    suspend fun updateStoreLogo(logoPath: String?)
}

