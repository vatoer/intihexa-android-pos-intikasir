package id.stargan.intikasir.data.security

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(private val context: Context) {

    private val TAG = "SecurePreferences"

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_activation_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create EncryptedSharedPreferences, fallback to normal", e)
        context.getSharedPreferences("activation_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveSerialNumber(serialNumber: String) {
        try {
            encryptedPrefs.edit().putString(KEY_SERIAL_NUMBER, serialNumber).apply()
            Log.d(TAG, "Serial number saved: ${serialNumber.take(10)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save serial number", e)
        }
    }

    fun getSerialNumber(): String? {
        return try {
            encryptedPrefs.getString(KEY_SERIAL_NUMBER, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get serial number", e)
            null
        }
    }

    fun saveActivationStatus(isActivated: Boolean) {
        try {
            encryptedPrefs.edit().putBoolean(KEY_ACTIVATION_STATUS, isActivated).apply()
            Log.d(TAG, "Activation status saved: $isActivated")

            // Verify immediately
            val saved = encryptedPrefs.getBoolean(KEY_ACTIVATION_STATUS, false)
            Log.d(TAG, "Verification - Status after save: $saved")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save activation status", e)
        }
    }

    fun isActivated(): Boolean {
        return try {
            val status = encryptedPrefs.getBoolean(KEY_ACTIVATION_STATUS, false)
            Log.d(TAG, "Read activation status: $status")
            status
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read activation status", e)
            false
        }
    }

    fun saveActivationSignature(signature: String) {
        try {
            encryptedPrefs.edit().putString(KEY_ACTIVATION_SIGNATURE, signature).apply()
            Log.d(TAG, "Signature saved: ${signature.take(20)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save signature", e)
        }
    }

    fun getActivationSignature(): String? {
        return try {
            encryptedPrefs.getString(KEY_ACTIVATION_SIGNATURE, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get signature", e)
            null
        }
    }

    fun saveActivationExpiry(expiry: Long) {
        try {
            encryptedPrefs.edit().putLong(KEY_ACTIVATION_EXPIRY, expiry).apply()
            Log.d(TAG, "Expiry saved: $expiry")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expiry", e)
        }
    }

    fun getActivationExpiry(): Long {
        return try {
            encryptedPrefs.getLong(KEY_ACTIVATION_EXPIRY, 0L)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get expiry", e)
            0L
        }
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun clearActivation() {
        try {
            encryptedPrefs.edit()
                .remove(KEY_SERIAL_NUMBER)
                .remove(KEY_ACTIVATION_STATUS)
                .remove(KEY_ACTIVATION_SIGNATURE)
                .remove(KEY_ACTIVATION_EXPIRY)
                .apply()
            Log.d(TAG, "Activation data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear activation", e)
        }
    }

    companion object {
        private const val KEY_SERIAL_NUMBER = "serial_number"
        private const val KEY_ACTIVATION_STATUS = "activation_status"
        private const val KEY_ACTIVATION_SIGNATURE = "activation_signature"
        private const val KEY_ACTIVATION_EXPIRY = "activation_expiry"
    }
}

