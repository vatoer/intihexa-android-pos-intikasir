package id.stargan.intikasir.feature.product.domain.repository

import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Product feature
 * Clean architecture - domain layer
 */
interface ProductRepository {

    /**
     * Get all products
     */
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Get products by category
     */
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    /**
     * Get product by ID
     */
    fun getProductById(productId: String): Flow<Product?>

    /**
     * Search products by name
     */
    fun searchProducts(query: String): Flow<List<Product>>

    /**
     * Insert or update product
     */
    suspend fun insertProduct(product: Product)

    /**
     * Update product
     */
    suspend fun updateProduct(product: Product)

    /**
     * Delete product
     */
    suspend fun deleteProduct(productId: String)

    /**
     * Get all categories
     */
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Insert category
     */
    suspend fun insertCategory(category: Category)

    /**
     * Update category
     */
    suspend fun updateCategory(category: Category)

    /**
     * Delete category
     */
    suspend fun deleteCategory(categoryId: String)
}

