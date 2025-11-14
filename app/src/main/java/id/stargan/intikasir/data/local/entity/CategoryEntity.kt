package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val order: Int = 0,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null
)

