package id.stargan.intikasir.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk produk
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("name")]
)
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,
    val description: String? = null,
    val price: Double,
    val cost: Double = 0.0, // Harga modal (optional)
    val sku: String? = null, // Stock Keeping Unit
    val barcode: String? = null,
    val imageUrl: String? = null,

    // Category
    val categoryId: String? = null,

    // Stock Management
    val trackStock: Boolean = true,
    val stock: Int = 0,
    val minStock: Int = 0,
    val lowStockThreshold: Int = 10,

    // Status
    val isActive: Boolean = true,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val isDeleted: Boolean = false
)

