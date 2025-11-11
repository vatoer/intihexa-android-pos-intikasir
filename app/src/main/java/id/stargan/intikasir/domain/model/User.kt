package id.stargan.intikasir.domain.model

/**
 * Domain model untuk User
 */
data class User(
    val id: String,
    val name: String,
    val pin: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

enum class UserRole {
    ADMIN,
    CASHIER;

    fun displayName(): String = when (this) {
        ADMIN -> "Admin"
        CASHIER -> "Kasir"
    }
}

