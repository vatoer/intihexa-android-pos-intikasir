package id.stargan.intikasir.feature.auth.domain.model

import id.stargan.intikasir.domain.model.User

/**
 * Sealed class untuk hasil autentikasi
 */
sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String, val errorType: AuthErrorType = AuthErrorType.UNKNOWN) : AuthResult()
    data object Loading : AuthResult()
    data object Idle : AuthResult()
}

/**
 * Tipe error autentikasi
 */
enum class AuthErrorType {
    INVALID_PIN,           // PIN salah
    USER_NOT_FOUND,       // User tidak ditemukan
    USER_INACTIVE,        // User tidak aktif
    PIN_TOO_SHORT,        // PIN kurang dari 4 digit
    PIN_TOO_LONG,         // PIN lebih dari 6 digit
    NETWORK_ERROR,        // Error jaringan (untuk future sync)
    DATABASE_ERROR,       // Error database
    UNKNOWN               // Error tidak diketahui
}

/**
 * Session state untuk track user yang sedang login
 */
data class AuthSession(
    val user: User,
    val loginTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

