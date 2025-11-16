package id.stargan.intikasir.feature.auth.data.source

import id.stargan.intikasir.data.local.dao.UserDao
import id.stargan.intikasir.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source untuk authentication
 * Handles database operations untuk user
 */
@Singleton
class AuthLocalDataSource @Inject constructor(
    private val userDao: UserDao
) {
    /**
     * Get user by hashed PIN
     * @param hashedPin Hashed PIN untuk authentication
     * @return UserEntity? user jika ditemukan, null jika tidak
     */
    suspend fun getUserByPin(hashedPin: String): UserEntity? {
        return userDao.getUserByPin(hashedPin)
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return UserEntity? user jika ditemukan, null jika tidak
     */
    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    /**
     * Get user by ID as Flow
     * @param userId User ID
     * @return Flow<UserEntity?> flow of user
     */
    fun getUserByIdFlow(userId: String): Flow<UserEntity?> {
        return userDao.getUserByIdFlow(userId)
    }

    /**
     * Get all active users
     * @return Flow<List<UserEntity>> flow of active users
     */
    fun getActiveUsers(): Flow<List<UserEntity>> {
        return userDao.getActiveUsers()
    }

    /**
     * Get all users
     * @return Flow<List<UserEntity>> flow of all users
     */
    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    /**
     * Insert or update user
     * @param user UserEntity to insert/update
     */
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    /**
     * Update user
     * @param user UserEntity to update
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    /**
     * Get user count (untuk check apakah ada user atau perlu setup awal)
     * @return Int jumlah user yang ada
     */
    suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }

    /**
     * Create a new user
     * @param user UserEntity to create
     */
    suspend fun createUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    /**
     * Toggle active status of a user
     * @param userId User ID
     * @param active New active status
     */
    suspend fun toggleActive(userId: String, active: Boolean) {
        userDao.updateActiveStatus(userId, active)
    }

    /**
     * Soft delete a user
     * @param userId User ID
     */
    suspend fun softDeleteUser(userId: String) {
        userDao.softDeleteUser(userId)
    }

    /**
     * Get user by username
     * @param username Username untuk authentication
     * @return UserEntity? user jika ditemukan, null jika tidak
     */
    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)
}
