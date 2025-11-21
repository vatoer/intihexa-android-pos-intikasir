package id.stargan.intikasir.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import id.stargan.intikasir.data.api.ActivationApiService
import id.stargan.intikasir.data.model.ActivationPayload
import id.stargan.intikasir.data.model.ActivationRequest
import id.stargan.intikasir.data.model.ActivationRequestPayload
import id.stargan.intikasir.data.security.RsaEncryption
import id.stargan.intikasir.data.security.SecurePreferences
import id.stargan.intikasir.data.security.SignatureVerifier

class ActivationRepository(
    private val apiService: ActivationApiService,
    private val securePrefs: SecurePreferences,
    private val context: Context
) {

    private val TAG = "ActivationRepository"
    private val gson = Gson()

    /**
     * Aktivasi device dengan serial number menggunakan encrypted request
     * @param serialNumber Serial number dari user
     * @return Result dengan pesan sukses atau error
     */
    suspend fun activateDevice(serialNumber: String): Result<String> {
        return try {
            val deviceId = securePrefs.getDeviceId()
            Log.d(TAG, "Starting activation - SN: $serialNumber, Device: $deviceId")

            // 1. Prepare payload
            val requestPayload = ActivationRequestPayload(
                sn = serialNumber,
                device_uuid = deviceId
            )
            val payloadJson = gson.toJson(requestPayload)

            // 2. Encrypt with public key (RSA-OAEP)
            val cipher = RsaEncryption.encrypt(payloadJson)
            Log.d(TAG, "Request encrypted, sending to server...")

            // 3. Send encrypted request
            val request = ActivationRequest(cipher = cipher)
            val response = apiService.activate(request)
            Log.d(TAG, "Server response received - ok: ${response.ok}")

            // 4. Check response
            if (response.ok && response.payload != null && response.signature != null) {
                // Decode payload
                val payloadDecoded = String(
                    Base64.decode(response.payload, Base64.DEFAULT),
                    Charsets.UTF_8
                )
                Log.d(TAG, "Payload decoded: $payloadDecoded")

                // Verify signature
                val isValid = SignatureVerifier.verifySignature(payloadDecoded, response.signature)
                Log.d(TAG, "Signature verification: $isValid")

                if (isValid) {
                    // Parse activation data
                    val activationData = gson.fromJson(payloadDecoded, ActivationPayload::class.java)
                    Log.d(TAG, "Activation data parsed - Expiry: ${activationData.expiry}")

                    // Save to encrypted preferences
                    Log.d(TAG, "Saving to SecurePreferences...")
                    securePrefs.saveSerialNumber(activationData.sn)
                    securePrefs.saveActivationStatus(true)
                    securePrefs.saveActivationSignature(response.signature)
                    securePrefs.saveActivationExpiry(activationData.expiry)

                    // Verify save
                    val savedStatus = securePrefs.isActivated()
                    Log.d(TAG, "Verification - Saved status: $savedStatus")

                    if (!savedStatus) {
                        Log.e(TAG, "ERROR: Status not saved properly!")
                        return Result.failure(Exception("Gagal menyimpan status aktivasi"))
                    }

                    Log.d(TAG, "Activation successful!")
                    Result.success(response.message ?: "Aktivasi berhasil")
                } else {
                    Log.e(TAG, "Signature verification failed")
                    Result.failure(Exception("Signature tidak valid. Aktivasi gagal."))
                }
            } else {
                val errorMsg = response.error ?: response.message ?: "Aktivasi gagal"
                Log.e(TAG, "Activation failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during activation", e)
            Result.failure(Exception("Gagal menghubungi server: ${e.message}"))
        }
    }

    /**
     * Cek apakah device sudah diaktivasi dan masih valid
     * @return true jika aktivasi valid, false jika tidak
     */
    fun isActivated(): Boolean {
        Log.d(TAG, "Checking if activated...")

        // Simple check - just check the boolean flag
        val isActivated = securePrefs.isActivated()
        Log.d(TAG, "Activation status from prefs: $isActivated")

        if (!isActivated) {
            Log.d(TAG, "Not activated")
            return false
        }

        // Check expiry only if set
        val expiry = securePrefs.getActivationExpiry()
        Log.d(TAG, "Expiry: $expiry, Current: ${System.currentTimeMillis()}")

        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            Log.d(TAG, "Activation expired, clearing...")
            securePrefs.clearActivation()
            return false
        }

        // Signature was already verified during activation
        // No need to verify again every time
        Log.d(TAG, "Activation is valid")
        return true
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

