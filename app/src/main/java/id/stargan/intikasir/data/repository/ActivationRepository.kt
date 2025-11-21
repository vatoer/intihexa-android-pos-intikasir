package id.stargan.intikasir.data.repository

import android.content.Context
import id.stargan.intikasir.data.api.ActivationApiService
import id.stargan.intikasir.data.model.ActivationRequest
import id.stargan.intikasir.data.security.SecurePreferences
import id.stargan.intikasir.data.security.SignatureVerifier

class ActivationRepository(
    private val apiService: ActivationApiService,
    private val securePrefs: SecurePreferences,
    private val context: Context
) {

    /**
     * Aktivasi device dengan serial number
     * @param serialNumber Serial number dari user
     * @return Result dengan pesan sukses atau error
     */
    suspend fun activateDevice(serialNumber: String): Result<String> {
        return try {
            val deviceId = securePrefs.getDeviceId()
            val request = ActivationRequest(serialNumber, deviceId)

            val response = apiService.activate(request)

            if (response.success && response.signature != null) {
                // Verify signature
                val dataToVerify = "$serialNumber:$deviceId:${response.expiry}"
                val isValid = SignatureVerifier.verifySignature(dataToVerify, response.signature)

                if (isValid) {
                    securePrefs.saveSerialNumber(serialNumber)
                    securePrefs.saveActivationStatus(true)
                    securePrefs.saveActivationSignature(response.signature)
                    response.expiry?.let { securePrefs.saveActivationExpiry(it) }

                    Result.success(response.message)
                } else {
                    Result.failure(Exception("Signature tidak valid. Aktivasi gagal."))
                }
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghubungi server: ${e.message}"))
        }
    }

    /**
     * Cek apakah device sudah diaktivasi dan masih valid
     * @return true jika aktivasi valid, false jika tidak
     */
    fun isActivated(): Boolean {
        val isActivated = securePrefs.isActivated()
        if (!isActivated) return false

        // Check expiry
        val expiry = securePrefs.getActivationExpiry()
        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            securePrefs.clearActivation()
            return false
        }

        // Verify signature still valid
        val serialNumber = securePrefs.getSerialNumber() ?: return false
        val deviceId = securePrefs.getDeviceId()
        val signature = securePrefs.getActivationSignature() ?: return false

        val dataToVerify = "$serialNumber:$deviceId:$expiry"
        return SignatureVerifier.verifySignature(dataToVerify, signature)
    }

    /**
     * Deaktivasi device (clear semua data aktivasi)
     */
    fun deactivate() {
        securePrefs.clearActivation()
    }

    /**
     * Get device ID (SSAID)
     */
    fun getDeviceId(): String {
        return securePrefs.getDeviceId()
    }

    /**
     * Get serial number yang tersimpan
     */
    fun getSerialNumber(): String? {
        return securePrefs.getSerialNumber()
    }

    /**
     * Get expiry timestamp
     */
    fun getActivationExpiry(): Long {
        return securePrefs.getActivationExpiry()
    }
}

