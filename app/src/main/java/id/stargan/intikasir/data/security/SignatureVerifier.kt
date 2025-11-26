package id.stargan.intikasir.data.security

import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object SignatureVerifier {

    private const val TAG = "SignatureVerifier"

    private fun isDebugBuild(): Boolean {
        return try {
            val cls = Class.forName("id.stargan.intikasir.BuildConfig")
            val field = cls.getField("DEBUG")
            field.getBoolean(null)
        } catch (e: Throwable) {
            // Fallback: consider non-debug
            false
        }
    }

    /**
     * Verifikasi signature menggunakan RSA SHA256
     * @param data Data asli yang ditandatangani (format: "serialNumber:deviceId:expiry")
     * @param signatureBase64 Signature dalam format Base64
     * @return true jika signature valid, false jika tidak
     */
    fun verifySignature(data: String, signatureBase64: String): Boolean {
        return try {
            val publicKey = getPublicKey()

            // Try common base64 variants (NO_WRAP recommended from server)
            val signatureBytes = try {
                Base64.decode(signatureBase64.trim(), Base64.NO_WRAP)
            } catch (ex: IllegalArgumentException) {
                if (isDebugBuild()) {
                    Log.d(TAG, "Base64 NO_WRAP decode failed, falling back to DEFAULT", ex)
                }
                Base64.decode(signatureBase64.trim(), Base64.DEFAULT)
            }

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(data.toByteArray(Charsets.UTF_8))

            signature.verify(signatureBytes)
        } catch (e: Exception) {
            if (isDebugBuild()) {
                Log.d(TAG, "verifySignature failed: ${e.message}", e)
            }
            false
        }
    }

    private fun getPublicKey(): PublicKey {
        // Read key from centralized provider and normalize (remove PEM headers/whitespace)
        val cleaned = PublicKeyProvider.PUBLIC_KEY_BASE64
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .trim()

        val keyBytes = Base64.decode(cleaned, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }
}
