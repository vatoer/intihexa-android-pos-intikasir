package id.stargan.intikasir.data.security

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_activation_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSerialNumber(serialNumber: String) {
        encryptedPrefs.edit().putString(KEY_SERIAL_NUMBER, serialNumber).apply()
    }

    fun getSerialNumber(): String? {
        return encryptedPrefs.getString(KEY_SERIAL_NUMBER, null)
    }

    fun saveActivationStatus(isActivated: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_ACTIVATION_STATUS, isActivated).apply()
    }

    fun isActivated(): Boolean {
        return encryptedPrefs.getBoolean(KEY_ACTIVATION_STATUS, false)
    }

    fun saveActivationSignature(signature: String) {
        encryptedPrefs.edit().putString(KEY_ACTIVATION_SIGNATURE, signature).apply()
    }

    fun getActivationSignature(): String? {
        return encryptedPrefs.getString(KEY_ACTIVATION_SIGNATURE, null)
    }

    fun saveActivationExpiry(expiry: Long) {
        encryptedPrefs.edit().putLong(KEY_ACTIVATION_EXPIRY, expiry).apply()
    }

    fun getActivationExpiry(): Long {
        return encryptedPrefs.getLong(KEY_ACTIVATION_EXPIRY, 0L)
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun clearActivation() {
        encryptedPrefs.edit()
            .remove(KEY_ACTIVATION_STATUS)
            .remove(KEY_ACTIVATION_SIGNATURE)
            .remove(KEY_ACTIVATION_EXPIRY)
            .apply()
    }

    companion object {
        private const val KEY_SERIAL_NUMBER = "serial_number"
        private const val KEY_ACTIVATION_STATUS = "activation_status"
        private const val KEY_ACTIVATION_SIGNATURE = "activation_signature"
        private const val KEY_ACTIVATION_EXPIRY = "activation_expiry"
    }
}

