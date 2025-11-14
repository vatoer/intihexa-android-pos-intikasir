package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Use case untuk delete category
 */
class DeleteCategoryUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(categoryId: String) {
        productRepository.deleteCategory(categoryId)
    }
}

