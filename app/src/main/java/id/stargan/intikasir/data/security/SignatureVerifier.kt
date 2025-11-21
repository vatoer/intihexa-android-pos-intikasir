package id.stargan.intikasir.data.security

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object SignatureVerifier {

    // Public key dalam Base64 (contoh RSA 2048-bit)
    // GANTI dengan public key yang sesuai dengan private key di server
    // Ini adalah contoh public key untuk demo
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
     * Verifikasi signature menggunakan RSA SHA256
     * @param data Data asli yang ditandatangani (format: "serialNumber:deviceId:expiry")
     * @param signatureBase64 Signature dalam format Base64
     * @return true jika signature valid, false jika tidak
     */
    fun verifySignature(data: String, signatureBase64: String): Boolean {
        return try {
            val publicKey = getPublicKey()
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(data.toByteArray(Charsets.UTF_8))

            signature.verify(signatureBytes)
        } catch (e: Exception) {
            // Log error in production
            false
        }
    }

    private fun getPublicKey(): PublicKey {
        val keyBytes = Base64.decode(PUBLIC_KEY_BASE64.trim().replace("\n", ""), Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }
}

