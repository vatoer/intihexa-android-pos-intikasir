package id.stargan.intikasir.feature.auth.data.mapper

import id.stargan.intikasir.data.local.entity.UserEntity
import id.stargan.intikasir.data.local.entity.UserRole as EntityUserRole
import id.stargan.intikasir.domain.model.User
import id.stargan.intikasir.domain.model.UserRole

/**
 * Mapper untuk convert antara UserEntity dan User domain model
 */
object UserMapper {

    /**
     * Convert UserEntity to User domain model
     */
    fun UserEntity.toDomainModel(): User {
        return User(
            id = this.id,
            name = this.name,
            pin = this.pin,
            role = this.role.toDomainRole(),
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            username = this.username
        )
    }

    /**
     * Convert User domain model to UserEntity
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            username = this.username,
            name = this.name,
            pin = this.pin,
            role = this.role.toEntityRole(),
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    /**
     * Convert List<UserEntity> to List<User>
     */
    fun List<UserEntity>.toDomainModels(): List<User> {
        return this.map { it.toDomainModel() }
    }

    /**
     * Convert EntityUserRole to domain UserRole
     */
    private fun EntityUserRole.toDomainRole(): UserRole {
        return when (this) {
            EntityUserRole.ADMIN -> UserRole.ADMIN
            EntityUserRole.CASHIER -> UserRole.CASHIER
        }
    }

    /**
     * Convert domain UserRole to EntityUserRole
     */
    private fun UserRole.toEntityRole(): EntityUserRole {
        return when (this) {
            UserRole.ADMIN -> EntityUserRole.ADMIN
            UserRole.CASHIER -> EntityUserRole.CASHIER
        }
    }
}
