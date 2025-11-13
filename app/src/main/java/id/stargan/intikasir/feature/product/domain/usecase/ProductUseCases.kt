package id.stargan.intikasir.feature.product.domain.usecase

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.getAllProducts()
}

class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<Product?> =
        repository.getProductById(productId)
}

class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(query: String): Flow<List<Product>> =
        repository.searchProducts(query)
}

class SaveProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        if (product.id.isEmpty()) {
            // New product
            repository.insertProduct(product)
        } else {
            // Update existing
            repository.updateProduct(product)
        }
    }
}

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: String) {
        repository.deleteProduct(productId)
    }
}

class GetLowStockProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.getLowStockProducts()
}

