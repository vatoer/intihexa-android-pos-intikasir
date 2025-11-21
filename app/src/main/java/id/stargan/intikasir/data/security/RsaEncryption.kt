package id.stargan.intikasir.data.security

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RsaEncryption {

    // Same public key as SignatureVerifier
    private const val PUBLIC_KEY_BASE64 = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5Ydic+nfpmy7vzd9kJsX
sz5cXp1Rvo/i5En17tD2LRFmJ09H9pMMkGlQz1w4tTTDs0MA6KTihW2z8AyUkZmA
aPuIQXiwIdyln/DwUZ7dxD8ZTCaAuoxsSoIjNQ/aiRKyzqwR7S6GdBYoyot/Nwo1
1gbeRcqm4HGKgIccdHcuPeea03W1fKibkKXv0Rzd/mhHgdNc/rRK7aXUyj7Kkhwa
g2jy8TX1kWTMh3gzW5g9VLmvI5CsNvyUihSeeSdN+xTnm3c/Gssl9xvrDKCAkBFs
6e1N0iw7SzG1VUY+GtpcucjUS+VsgF3IS4E6tQqfrfoIgQaJrkJyjlYAsY00dgnT
VQIDAQAB
"""

    /**
     * Encrypt data using RSA-OAEP with SHA-256
     * @param data Plain text to encrypt
     * @return Base64 encoded encrypted data
     */
    fun encrypt(data: String): String {
        return try {
            val publicKey = getPublicKey()
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt data: ${e.message}", e)
        }
    }

    /**
     * Decrypt base64 encoded data (for testing/verification only)
     * Note: In production, decryption happens on server side
     */
    fun decrypt(encryptedBase64: String, privateKeyPem: String): String {
        // This is for testing only - normally we don't have private key on client
        throw UnsupportedOperationException("Decryption should only happen on server")
    }

    private fun getPublicKey(): PublicKey {
        val keyBytes = Base64.decode(
            PUBLIC_KEY_BASE64.trim().replace("\n", ""),
            Base64.DEFAULT
        )
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }
}

