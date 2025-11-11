package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.StoreSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreSettingsDao {

    @Query("SELECT * FROM store_settings WHERE id = 'store_settings' LIMIT 1")
    fun getStoreSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 'store_settings' LIMIT 1")
    suspend fun getStoreSettingsSuspend(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: StoreSettingsEntity)

    @Update
    suspend fun updateSettings(settings: StoreSettingsEntity)

    @Query("UPDATE store_settings SET syncedAt = :timestamp WHERE id = 'store_settings'")
    suspend fun markAsSynced(timestamp: Long)
}

