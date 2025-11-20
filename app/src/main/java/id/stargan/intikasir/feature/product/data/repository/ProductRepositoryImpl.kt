package id.stargan.intikasir.feature.product.data.repository

import android.util.Log
import id.stargan.intikasir.data.local.dao.CategoryDao
import id.stargan.intikasir.data.local.dao.ProductDao
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.data.mapper.ProductMapper.toDomain
import id.stargan.intikasir.feature.product.data.mapper.ProductMapper.toEntity
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { entity ->
                // CategoryName akan di-resolve di ViewModel jika diperlukan
                entity.toDomain(null)
            }
        }
    }

    override fun getProductById(productId: String): Flow<Product?> {
        return productDao.getProductByIdFlow(productId).map { entity ->
            entity?.toDomain(null)
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain(null) }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { entity ->
                entity.toDomain(null)
            }
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { entity ->
                entity.toDomain(null)
            }
        }
    }

    override suspend fun insertProduct(product: Product) {
        try {
            Log.d("ProductRepository", "===== INSERT/UPDATE PRODUCT DEBUG =====")
            Log.d("ProductRepository", "Product ID: ${product.id}")
            Log.d("ProductRepository", "Product Name: ${product.name}")
            Log.d("ProductRepository", "Category ID: ${product.categoryId}")

            // Check if category exists if categoryId is not null
            if (!product.categoryId.isNullOrBlank()) {
                val categoryExists = categoryDao.getCategoryById(product.categoryId)
                Log.d("ProductRepository", "Category exists check: $categoryExists")
                if (categoryExists == null) {
                    val errorMsg = "FOREIGN KEY ERROR: Category with ID '${product.categoryId}' does not exist!"
                    Log.e("ProductRepository", errorMsg)
                    throw IllegalStateException(errorMsg)
                }
            } else {
                Log.d("ProductRepository", "No category selected (null/blank)")
            }

            val entity = product.copy(
                id = if (product.id.isEmpty()) UUID.randomUUID().toString() else product.id,
                updatedAt = System.currentTimeMillis()
            ).toEntity()

            Log.d("ProductRepository", "Entity to insert/update - categoryId: ${entity.categoryId}")

            // Check if product already exists
            val existingProduct = productDao.getProductById(entity.id)
            if (existingProduct != null) {
                Log.d("ProductRepository", "Product exists, using UPDATE instead of INSERT REPLACE to avoid FK constraint")
                // Use UPDATE to avoid REPLACE strategy which triggers DELETE then INSERT
                // This prevents FOREIGN KEY RESTRICT error from TransactionItems
                productDao.updateProduct(entity)
                Log.d("ProductRepository", "Product updated successfully")
            } else {
                Log.d("ProductRepository", "New product, using INSERT")
                productDao.insertProduct(entity)
                Log.d("ProductRepository", "Product inserted successfully")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Failed to insert/update product: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateProduct(product: Product) {
        val entity = product.copy(
            updatedAt = System.currentTimeMillis()
        ).toEntity()
        productDao.updateProduct(entity)
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.softDeleteProduct(productId)
    }

    // Category operations
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategoryById(categoryId: String): Flow<Category?> {
        return categoryDao.getCategoryByIdFlow(categoryId).map { it?.toDomain() }
    }

    override suspend fun insertCategory(category: Category) {
        val entity = category.copy(
            id = if (category.id.isEmpty()) UUID.randomUUID().toString() else category.id,
            updatedAt = System.currentTimeMillis()
        ).toEntity()
        categoryDao.insertCategory(entity)
    }

    override suspend fun updateCategory(category: Category) {
        val entity = category.copy(
            updatedAt = System.currentTimeMillis()
        ).toEntity()
        categoryDao.updateCategory(entity)
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.softDeleteCategory(categoryId)
    }
}

