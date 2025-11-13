package id.stargan.intikasir.feature.product.domain.repository

import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Product management
 */
interface ProductRepository {

    // Product operations
    fun getAllProducts(): Flow<List<Product>>
    fun getProductById(productId: String): Flow<Product?>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)

    // Category operations
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(categoryId: String): Flow<Category?>
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
}

