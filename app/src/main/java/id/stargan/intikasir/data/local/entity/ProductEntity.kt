package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val cost: Double,
    val stock: Int,
    val minStock: Int = 0,
    val lowStockThreshold: Int = 0,
    val categoryId: String?,
    val sku: String?,
    val barcode: String?,
    val imageUrl: String?,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null
)

