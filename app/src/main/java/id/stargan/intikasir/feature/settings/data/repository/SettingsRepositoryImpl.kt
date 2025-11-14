package id.stargan.intikasir.feature.settings.data.repository

import id.stargan.intikasir.data.local.dao.StoreSettingsDao
import id.stargan.intikasir.data.local.entity.StoreSettingsEntity
import id.stargan.intikasir.domain.model.StoreSettings
import id.stargan.intikasir.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val storeSettingsDao: StoreSettingsDao
) : SettingsRepository {

    override fun getStoreSettings(): Flow<StoreSettings?> {
        return storeSettingsDao.getStoreSettings().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun updateStoreSettings(settings: StoreSettings) {
        storeSettingsDao.insertSettings(settings.toEntity())
    }

    override suspend fun updateStoreLogo(logoPath: String?) {
        val current = storeSettingsDao.getStoreSettingsSuspend() ?: StoreSettingsEntity()
        storeSettingsDao.insertSettings(
            current.copy(
                storeLogo = logoPath,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun StoreSettingsEntity.toDomain() = StoreSettings(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        storePhone = storePhone,
        storeEmail = storeEmail,
        storeLogo = storeLogo,
        taxEnabled = taxEnabled,
        taxPercentage = taxPercentage,
        taxName = taxName,
        serviceEnabled = serviceEnabled,
        servicePercentage = servicePercentage,
        serviceName = serviceName,
        receiptHeader = receiptHeader,
        receiptFooter = receiptFooter,
        printLogo = printLogo,
        printerName = printerName,
        printerAddress = printerAddress,
        printerConnected = printerConnected,
        currencySymbol = currencySymbol,
        currencyCode = currencyCode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun StoreSettings.toEntity() = StoreSettingsEntity(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        storePhone = storePhone,
        storeEmail = storeEmail,
        storeLogo = storeLogo,
        taxEnabled = taxEnabled,
        taxPercentage = taxPercentage,
        taxName = taxName,
        serviceEnabled = serviceEnabled,
        servicePercentage = servicePercentage,
        serviceName = serviceName,
        receiptHeader = receiptHeader,
        receiptFooter = receiptFooter,
        printLogo = printLogo,
        printerName = printerName,
        printerAddress = printerAddress,
        printerConnected = printerConnected,
        currencySymbol = currencySymbol,
        currencyCode = currencyCode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

