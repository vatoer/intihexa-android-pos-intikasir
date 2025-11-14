package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk get all products
 */
class GetAllProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getAllProducts()
    }
}

