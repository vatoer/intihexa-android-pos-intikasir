package id.stargan.intikasir.domain.model

/**
 * Domain model untuk Product dengan informasi kategori
 */
data class Product(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val cost: Double = 0.0,
    val sku: String? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val trackStock: Boolean = true,
    val stock: Int = 0,
    val lowStockThreshold: Int = 5,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isLowStock: Boolean
        get() = trackStock && stock <= lowStockThreshold

    val isOutOfStock: Boolean
        get() = trackStock && stock == 0

    val formattedPrice: String
        get() = formatCurrency(price)

    private fun formatCurrency(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount)}"
    }
}

/**
 * Domain model untuk Category
 */
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

