package id.stargan.intikasir.feature.auth.data.repository

import id.stargan.intikasir.domain.model.User
import id.stargan.intikasir.domain.model.UserRole
import id.stargan.intikasir.feature.auth.data.mapper.UserMapper.toDomainModel
import id.stargan.intikasir.feature.auth.data.mapper.UserMapper.toDomainModels
import id.stargan.intikasir.feature.auth.data.source.AuthLocalDataSource
import id.stargan.intikasir.feature.auth.data.source.AuthPreferencesDataSource
import id.stargan.intikasir.feature.auth.domain.model.AuthErrorType
import id.stargan.intikasir.feature.auth.domain.model.AuthResult
import id.stargan.intikasir.feature.auth.domain.model.AuthSession
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository
 * Handles authentication logic dengan clean architecture
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val localDataSource: AuthLocalDataSource,
    private val preferencesDataSource: AuthPreferencesDataSource
) : AuthRepository {

    override suspend fun login(pin: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)

            // Validate PIN format first
            val validation = validatePinFormat(pin)
            if (validation.isFailure) {
                emit(
                    AuthResult.Error(
                        message = validation.exceptionOrNull()?.message ?: "Invalid PIN format",
                        errorType = AuthErrorType.PIN_TOO_SHORT
                    )
                )
                return@flow
            }

            // Hash PIN
            val hashedPin = hashPin(pin)

            // Get user by hashed PIN
            val userEntity = localDataSource.getUserByPin(hashedPin)

            if (userEntity == null) {
                emit(
                    AuthResult.Error(
                        message = "PIN tidak valid atau user tidak ditemukan",
                        errorType = AuthErrorType.USER_NOT_FOUND
                    )
                )
                return@flow
            }

            // Check if user is active
            if (!userEntity.isActive) {
                emit(
                    AuthResult.Error(
                        message = "User tidak aktif. Hubungi administrator.",
                        errorType = AuthErrorType.USER_INACTIVE
                    )
                )
                return@flow
            }

            // Save login session
            preferencesDataSource.saveLoginSession(
                userId = userEntity.id,
                loginTime = System.currentTimeMillis()
            )

            // Convert to domain model and emit success
            val user = userEntity.toDomainModel()
            emit(AuthResult.Success(user))

        } catch (e: Exception) {
            emit(
                AuthResult.Error(
                    message = "Terjadi kesalahan: ${e.message}",
                    errorType = AuthErrorType.DATABASE_ERROR
                )
            )
        }
    }

    override suspend fun logout() {
        preferencesDataSource.clearLoginSession()
    }

    override fun getCurrentUser(): Flow<User?> {
        return preferencesDataSource.getCurrentUserId().map { userId ->
            if (userId != null) {
                localDataSource.getUserById(userId)?.toDomainModel()
            } else {
                null
            }
        }
    }

    override fun getCurrentSession(): Flow<AuthSession?> {
        return combine(
            getCurrentUser(),
            preferencesDataSource.getLoginTime(),
            preferencesDataSource.isLoggedIn()
        ) { user, loginTime, isLoggedIn ->
            if (user != null && loginTime != null && isLoggedIn) {
                AuthSession(
                    user = user,
                    loginTime = loginTime,
                    isActive = true
                )
            } else {
                null
            }
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return preferencesDataSource.isLoggedIn()
    }

    override fun validatePinFormat(pin: String): Result<Unit> {
        return when {
            pin.isBlank() -> {
                Result.failure(IllegalArgumentException("PIN tidak boleh kosong"))
            }
            !pin.all { it.isDigit() } -> {
                Result.failure(IllegalArgumentException("PIN harus berupa angka"))
            }
            pin.length < 4 -> {
                Result.failure(IllegalArgumentException("PIN minimal 4 digit"))
            }
            pin.length > 6 -> {
                Result.failure(IllegalArgumentException("PIN maksimal 6 digit"))
            }
            else -> {
                Result.success(Unit)
            }
        }
    }

    override fun getUsersByRole(role: UserRole): Flow<List<User>> {
        return localDataSource.getActiveUsers().map { userEntities ->
            userEntities
                .filter { it.role.name == role.name }
                .toDomainModels()
        }
    }

    override fun hashPin(pin: String): String {
        // Use SHA-256 for PIN hashing
        // In production, consider using bcrypt or argon2 for better security
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.fold("") { str, byte -> str + "%02x".format(byte) }
    }

    override fun verifyPin(pin: String, hashedPin: String): Boolean {
        return hashPin(pin) == hashedPin
    }

    override suspend fun initializeDefaultUsers() {
        // Hanya inisialisasi jika belum ada user
        if (!hasUsers()) {
            val defaultUsers = createDefaultUsers()
            defaultUsers.forEach { user ->
                localDataSource.insertUser(user)
            }
        }
    }

    override suspend fun hasUsers(): Boolean {
        return localDataSource.getUserCount() > 0
    }

    /**
     * Create default users untuk inisialisasi awal
     * Admin PIN: 1234
     * Kasir PIN: 5678
     */
    private fun createDefaultUsers(): List<id.stargan.intikasir.data.local.entity.UserEntity> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            id.stargan.intikasir.data.local.entity.UserEntity(
                id = java.util.UUID.randomUUID().toString(),
                username = "admin",
                name = "Admin",
                pin = hashPin("1234"),
                role = id.stargan.intikasir.data.local.entity.UserRole.ADMIN,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            id.stargan.intikasir.data.local.entity.UserEntity(
                id = java.util.UUID.randomUUID().toString(),
                username = "kasir1",
                name = "Kasir 1",
                pin = hashPin("5678"),
                role = id.stargan.intikasir.data.local.entity.UserRole.CASHIER,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        )
    }

    override suspend fun updateUserProfile(userId: String, newName: String, newHashedPin: String?) {
        // Fetch current user entity
        val userEntity = localDataSource.getUserById(userId) ?: throw IllegalArgumentException("User tidak ditemukan")
        val updated = userEntity.copy(
            name = newName,
            pin = newHashedPin ?: userEntity.pin,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.updateUser(updated)
        // If current session user changed name, session remains valid (DataStore stores userId only)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return localDataSource.getAllUsers().map { it.toDomainModels() }
    }

    override suspend fun createUser(username: String, name: String, role: UserRole, hashedPin: String): User {
        val entity = id.stargan.intikasir.data.local.entity.UserEntity(
            username = username,
            name = name,
            pin = hashedPin,
            role = id.stargan.intikasir.data.local.entity.UserRole.valueOf(role.name)
        )
        localDataSource.createUser(entity)
        return entity.toDomainModel()
    }

    override suspend fun toggleUserActive(userId: String, active: Boolean) {
        localDataSource.toggleActive(userId, active)
    }

    override suspend fun softDeleteUser(userId: String) {
        localDataSource.softDeleteUser(userId)
    }

    override suspend fun resetUserPin(userId: String, newHashedPin: String) {
        val user = localDataSource.getUserById(userId) ?: throw IllegalArgumentException("User tidak ditemukan")
        localDataSource.updateUser(user.copy(pin = newHashedPin, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun findUserByUsername(username: String): User? {
        return localDataSource.getUserByUsername(username)?.toDomainModel()
    }

    override suspend fun loginWithUser(userId: String, pin: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            val validation = validatePinFormat(pin)
            if (validation.isFailure) {
                emit(AuthResult.Error(validation.exceptionOrNull()?.message ?: "Invalid PIN", id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.PIN_TOO_SHORT))
                return@flow
            }
            val userEntity = localDataSource.getUserById(userId)
            if (userEntity == null || userEntity.isDeleted) {
                emit(AuthResult.Error("User tidak ditemukan", id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.USER_NOT_FOUND))
                return@flow
            }
            if (!userEntity.isActive) {
                emit(AuthResult.Error("User tidak aktif", id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.USER_INACTIVE))
                return@flow
            }
            if (!verifyPin(pin, userEntity.pin)) {
                emit(AuthResult.Error("PIN salah", id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.INVALID_PIN))
                return@flow
            }
            preferencesDataSource.saveLoginSession(userId = userEntity.id, loginTime = System.currentTimeMillis())
            emit(AuthResult.Success(userEntity.toDomainModel()))
        } catch (e: Exception) {
            emit(AuthResult.Error("Terjadi kesalahan: ${e.message}", id.stargan.intikasir.feature.auth.domain.model.AuthErrorType.DATABASE_ERROR))
        }
    }

    override suspend fun updateUserAccount(userId: String, username: String, name: String) {
        val userEntity = localDataSource.getUserById(userId) ?: throw IllegalArgumentException("User tidak ditemukan")
        val updated = userEntity.copy(
            username = username,
            name = name,
            updatedAt = System.currentTimeMillis()
        )
        localDataSource.updateUser(updated)
    }
}
