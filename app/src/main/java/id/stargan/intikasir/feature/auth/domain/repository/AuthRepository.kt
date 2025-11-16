package id.stargan.intikasir.feature.auth.domain.repository

import id.stargan.intikasir.domain.model.User
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.domain.model.AuthResult
import id.stargan.intikasir.feature.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk authentication
 * Menggunakan clean architecture principles
 */
interface AuthRepository {

    /**
     * Login user dengan PIN
     * @param pin PIN yang dimasukkan user (akan di-hash di repository)
     * @return Flow<AuthResult> untuk observe hasil login
     */
    suspend fun login(pin: String): Flow<AuthResult>

    /**
     * Logout user dan clear session
     */
    suspend fun logout()

    /**
     * Get current logged in user
     * @return Flow<User?> user yang sedang login atau null
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Get current auth session
     * @return Flow<AuthSession?> session yang sedang aktif atau null
     */
    fun getCurrentSession(): Flow<AuthSession?>

    /**
     * Check apakah user sudah login
     * @return Flow<Boolean> true jika sudah login
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * Validate PIN format (4-6 digit)
     * @param pin PIN yang akan divalidasi
     * @return Result<Unit> success jika valid, error jika tidak
     */
    fun validatePinFormat(pin: String): Result<Unit>

    /**
     * Get all users by role (untuk admin manage users)
     * @param role UserRole filter
     * @return Flow<List<User>> list of users
     */
    fun getUsersByRole(role: UserRole): Flow<List<User>>

    /**
     * Hash PIN dengan algoritma yang aman
     * @param pin PIN plain text
     * @return String hashed PIN
     */
    fun hashPin(pin: String): String

    /**
     * Verify PIN dengan hash yang tersimpan
     * @param pin PIN plain text
     * @param hashedPin Hashed PIN dari database
     * @return Boolean true jika match
     */
    fun verifyPin(pin: String, hashedPin: String): Boolean

    /**
     * Initialize default users jika database masih kosong
     * Dipanggil saat aplikasi pertama kali dijalankan
     */
    suspend fun initializeDefaultUsers()

    /**
     * Check apakah sudah ada user di database
     * @return Boolean true jika sudah ada user
     */
    suspend fun hasUsers(): Boolean

    /**
     * Update user profile (name and optionally PIN)
     */
    suspend fun updateUserProfile(userId: String, newName: String, newHashedPin: String? = null)

    /** Create new user */
    suspend fun createUser(username: String, name: String, role: UserRole, hashedPin: String): User
    /** Toggle active status */
    suspend fun toggleUserActive(userId: String, active: Boolean)
    /** Soft delete user */
    suspend fun softDeleteUser(userId: String)
    /** Reset pin */
    suspend fun resetUserPin(userId: String, newHashedPin: String)
    /** Get all users */
    fun getAllUsers(): Flow<List<User>>

    /**
     * Find user by username
     * @param username Username yang akan dicari
     * @return User? user yang ditemukan atau null
     */
    suspend fun findUserByUsername(username: String): User?

    /**
     * Login dengan menggunakan userId dan PIN
     * @param userId ID user yang akan digunakan untuk login
     * @param pin PIN yang dimasukkan user
     * @return Flow<AuthResult> untuk observe hasil login
     */
    suspend fun loginWithUser(userId: String, pin: String): Flow<AuthResult>

    /** Update username and name */
    suspend fun updateUserAccount(userId: String, username: String, name: String)
}
