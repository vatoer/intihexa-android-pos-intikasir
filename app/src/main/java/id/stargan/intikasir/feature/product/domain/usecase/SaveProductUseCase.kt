package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk save product (insert or update)
 */
class SaveProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        if (product.id.isEmpty()) {
            // New product - insert
            productRepository.insertProduct(product)
        } else {
            // Existing product - update
            productRepository.updateProduct(product)
        }
    }
}

