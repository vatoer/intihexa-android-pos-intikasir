package id.stargan.intikasir.feature.product.domain.model

/**
 * Sort options untuk product list
 */
enum class ProductSortBy {
    NAME_ASC,           // A-Z
    NAME_DESC,          // Z-A
    PRICE_ASC,          // Termurah
    PRICE_DESC,         // Termahal
    STOCK_ASC,          // Stok terendah
    STOCK_DESC,         // Stok tertinggi
    NEWEST,             // Terbaru
    OLDEST              // Terlama
}

/**
 * Filter options untuk product list
 */
data class ProductFilter(
    val categoryId: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val inStockOnly: Boolean = false,
    val lowStockOnly: Boolean = false,
    val activeOnly: Boolean = true
)

