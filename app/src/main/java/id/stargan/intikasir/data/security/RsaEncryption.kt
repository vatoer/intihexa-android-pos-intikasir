package id.stargan.intikasir.data.security

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RsaEncryption {

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

    private fun getPublicKey(): PublicKey {
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
