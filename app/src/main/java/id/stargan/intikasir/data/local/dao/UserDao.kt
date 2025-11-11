package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE pin = :hashedPin AND isDeleted = 0 AND isActive = 1 LIMIT 1")
    suspend fun getUserByPin(hashedPin: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE isDeleted = 0")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isDeleted = 1, updatedAt = :timestamp WHERE id = :userId")
    suspend fun softDeleteUser(userId: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE users SET syncedAt = :timestamp WHERE id = :userId")
    suspend fun markAsSynced(userId: String, timestamp: Long)

    @Query("SELECT * FROM users WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedUsers(): List<UserEntity>
}

