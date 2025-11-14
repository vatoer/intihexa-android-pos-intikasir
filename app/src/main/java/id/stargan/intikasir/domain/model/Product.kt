package id.stargan.intikasir.domain.model

import java.text.NumberFormat
import java.util.Locale

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
    val minStock: Int?,
    val lowStockThreshold: Int?,
    val imageUrl: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * Check if product is out of stock
     */
    val isOutOfStock: Boolean
        get() = stock <= 0

    /**
     * Check if product has low stock
     */
    val isLowStock: Boolean
        get() = !isOutOfStock && stock <= (lowStockThreshold ?: 10)

    /**
     * Get formatted price in Rupiah
     */
    val formattedPrice: String
        get() = formatRupiah(price)

    /**
     * Get formatted cost in Rupiah
     */
    val formattedCost: String
        get() = formatRupiah(cost ?: 0.0)

    private fun formatRupiah(amount: Double): String {
        val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val format = NumberFormat.getCurrencyInstance(locale)
        return format.format(amount).replace("Rp", "Rp ")
    }
}

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

