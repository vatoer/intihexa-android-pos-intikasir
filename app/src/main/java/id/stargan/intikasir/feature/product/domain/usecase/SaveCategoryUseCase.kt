package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk save category (insert or update)
 * Uses upsert pattern - Room's REPLACE strategy handles both insert and update
 */
class SaveCategoryUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(category: Category) {
        // Room's @Insert with OnConflictStrategy.REPLACE handles both insert and update
        productRepository.insertCategory(category)
    }
}

