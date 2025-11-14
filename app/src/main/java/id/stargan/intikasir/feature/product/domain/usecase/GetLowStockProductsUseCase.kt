package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case untuk get low stock products
 */
class GetLowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAllProducts().map { products ->
            products.filter { it.isLowStock }
        }
    }
}

