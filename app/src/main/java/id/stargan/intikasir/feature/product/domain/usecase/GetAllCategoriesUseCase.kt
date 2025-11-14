package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk get all categories
 */
class GetAllCategoriesUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return productRepository.getAllCategories()
    }
}

