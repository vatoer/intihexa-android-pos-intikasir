package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk menyimpan data pengguna (Admin dan Kasir)
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val username: String = "",
    val name: String,
    val pin: String, // Hashed PIN 4 digit
    val role: UserRole,
    val isActive: Boolean = true,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)

enum class UserRole {
    ADMIN,
    CASHIER
}
