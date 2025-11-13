package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.model.ProductFilter
import id.stargan.intikasir.feature.product.domain.model.ProductSortBy
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case untuk get dan filter products
 */
class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(
        filter: ProductFilter = ProductFilter(),
        sortBy: ProductSortBy = ProductSortBy.NAME_ASC
    ): Flow<List<Product>> {
        return productRepository.getAllProducts()
            .map { products ->
                var filteredProducts = products

                // Apply filters
                if (filter.categoryId != null) {
                    filteredProducts = filteredProducts.filter { it.categoryId == filter.categoryId }
                }

                if (filter.minPrice != null) {
                    filteredProducts = filteredProducts.filter { it.price >= filter.minPrice }
                }

                if (filter.maxPrice != null) {
                    filteredProducts = filteredProducts.filter { it.price <= filter.maxPrice }
                }

                if (filter.inStockOnly) {
                    filteredProducts = filteredProducts.filter { !it.isOutOfStock }
                }

                if (filter.lowStockOnly) {
                    filteredProducts = filteredProducts.filter { it.isLowStock }
                }

                if (filter.activeOnly) {
                    filteredProducts = filteredProducts.filter { it.isActive }
                }

                // Apply sorting
                when (sortBy) {
                    ProductSortBy.NAME_ASC -> filteredProducts.sortedBy { it.name.lowercase() }
                    ProductSortBy.NAME_DESC -> filteredProducts.sortedByDescending { it.name.lowercase() }
                    ProductSortBy.PRICE_ASC -> filteredProducts.sortedBy { it.price }
                    ProductSortBy.PRICE_DESC -> filteredProducts.sortedByDescending { it.price }
                    ProductSortBy.STOCK_ASC -> filteredProducts.sortedBy { it.stock }
                    ProductSortBy.STOCK_DESC -> filteredProducts.sortedByDescending { it.stock }
                    ProductSortBy.NEWEST -> filteredProducts.sortedByDescending { it.createdAt }
                    ProductSortBy.OLDEST -> filteredProducts.sortedBy { it.createdAt }
                }
            }
    }
}

