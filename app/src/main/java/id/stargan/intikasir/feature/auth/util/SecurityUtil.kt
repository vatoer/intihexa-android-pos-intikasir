package id.stargan.intikasir.feature.auth.util

import java.security.MessageDigest

/**
 * Security utilities untuk authentication
 */
object SecurityUtil {

    /**
     * Hash PIN dengan SHA-256
     * Note: In production, use stronger algorithm like bcrypt or argon2
     *
     * @param pin Plain text PIN
     * @return Hashed PIN as hex string
     */
    fun hashPin(pin: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw SecurityException("Failed to hash PIN: ${e.message}")
        }
    }

    /**
     * Verify PIN dengan hash yang tersimpan
     *
     * @param pin Plain text PIN
     * @param hashedPin Hashed PIN dari database
     * @return true jika match
     */
    fun verifyPin(pin: String, hashedPin: String): Boolean {
        return try {
            hashPin(pin) == hashedPin
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate random PIN untuk development/testing
     *
     * @param length Panjang PIN (default 4)
     * @return Random numeric PIN
     */
    fun generateRandomPin(length: Int = 4): String {
        return (1..length)
            .map { (0..9).random() }
            .joinToString("")
    }
}

