package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk delete product
 */
class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: String) {
        productRepository.deleteProduct(productId)
    }
}

