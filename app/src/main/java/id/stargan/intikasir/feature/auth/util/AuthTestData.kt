package id.stargan.intikasir.feature.auth.util

import id.stargan.intikasir.data.local.entity.UserEntity
import id.stargan.intikasir.data.local.entity.UserRole
import id.stargan.intikasir.domain.model.User
import java.util.UUID

/**
 * Test data dan utilities untuk development dan testing
 */
object AuthTestData {

    /**
     * Sample admin user untuk development
     * PIN: 1234
     */
    fun createSampleAdminEntity(): UserEntity {
        return UserEntity(
            id = UUID.randomUUID().toString(),
            name = "Admin",
            pin = SecurityUtil.hashPin("1234"), // Hashed PIN for "1234"
            role = UserRole.ADMIN,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Sample cashier user untuk development
     * PIN: 5678
     */
    fun createSampleCashierEntity(): UserEntity {
        return UserEntity(
            id = UUID.randomUUID().toString(),
            name = "Kasir 1",
            pin = SecurityUtil.hashPin("5678"), // Hashed PIN for "5678"
            role = UserRole.CASHIER,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Create multiple sample users
     */
    fun createSampleUsers(): List<UserEntity> {
        return listOf(
            createSampleAdminEntity(),
            createSampleCashierEntity(),
            UserEntity(
                id = UUID.randomUUID().toString(),
                name = "Kasir 2",
                pin = SecurityUtil.hashPin("9999"),
                role = UserRole.CASHIER,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                name = "Manager",
                pin = SecurityUtil.hashPin("0000"),
                role = UserRole.ADMIN,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Sample domain user for testing
     */
    fun createSampleDomainUser(): User {
        return User(
            id = UUID.randomUUID().toString(),
            name = "Test User",
            pin = SecurityUtil.hashPin("1234"),
            role = id.stargan.intikasir.domain.model.UserRole.ADMIN,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}

