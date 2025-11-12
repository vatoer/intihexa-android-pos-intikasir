package id.stargan.intikasir.feature.auth.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property untuk DataStore
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

/**
 * DataStore untuk menyimpan authentication session
 * Uses DataStore Preferences untuk reactive data storage
 */
@Singleton
class AuthPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.authDataStore

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_LOGIN_TIME = longPreferencesKey("login_time")
        private val KEY_LAST_ACTIVITY = longPreferencesKey("last_activity")
    }

    /**
     * Save login session
     * @param userId ID user yang login
     * @param loginTime Waktu login
     */
    suspend fun saveLoginSession(userId: String, loginTime: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_LOGIN_TIME] = loginTime
            preferences[KEY_LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    /**
     * Clear login session (logout)
     */
    suspend fun clearLoginSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Get current logged in user ID
     * @return Flow<String?> user ID atau null jika tidak ada
     */
    fun getCurrentUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[KEY_USER_ID]
        }
    }

    /**
     * Check if user is logged in
     * @return Flow<Boolean> true jika logged in
     */
    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_IS_LOGGED_IN] ?: false
        }
    }

    /**
     * Get login time
     * @return Flow<Long?> login timestamp atau null
     */
    fun getLoginTime(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[KEY_LOGIN_TIME]
        }
    }

    /**
     * Update last activity time (untuk auto-logout feature)
     */
    suspend fun updateLastActivity() {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    /**
     * Get last activity time
     * @return Flow<Long?> last activity timestamp atau null
     */
    fun getLastActivity(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[KEY_LAST_ACTIVITY]
        }
    }
}

