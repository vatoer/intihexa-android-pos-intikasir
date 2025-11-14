package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk save product (insert or update)
 * Uses upsert pattern - Room's REPLACE strategy handles both insert and update
 */
class SaveProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        // Room's @Insert with OnConflictStrategy.REPLACE handles both insert and update
        productRepository.insertProduct(product)
    }
}

