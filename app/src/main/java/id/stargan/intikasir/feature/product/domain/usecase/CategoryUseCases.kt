package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCategoriesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Category>> = repository.getAllCategories()
}

class GetCategoryByIdUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(categoryId: String): Flow<Category?> =
        repository.getCategoryById(categoryId)
}

class SaveCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(category: Category) {
        if (category.id.isEmpty()) {
            repository.insertCategory(category)
        } else {
            repository.updateCategory(category)
        }
    }
}

class DeleteCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(categoryId: String) {
        repository.deleteCategory(categoryId)
    }
}

