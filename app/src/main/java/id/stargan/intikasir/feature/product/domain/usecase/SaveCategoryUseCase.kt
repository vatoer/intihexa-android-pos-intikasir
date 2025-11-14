package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk save category (insert or update)
 */
class SaveCategoryUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(category: Category) {
        if (category.id.isEmpty()) {
            // New category - insert
            productRepository.insertCategory(category)
        } else {
            // Existing category - update
            productRepository.updateCategory(category)
        }
    }
}

