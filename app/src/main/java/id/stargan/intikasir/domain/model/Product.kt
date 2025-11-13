package id.stargan.intikasir.domain.model

/**
 * Domain model untuk Product
 */
data class Product(
    val id: String,
    val name: String,
    val sku: String?,
    val barcode: String?,
    val categoryId: String?,
    val categoryName: String?,
    val description: String?,
    val price: Double,
    val cost: Double?,
    val stock: Int,
    val minStock: Int,
    val imageUrl: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Domain model untuk Category
 */
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val order: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

