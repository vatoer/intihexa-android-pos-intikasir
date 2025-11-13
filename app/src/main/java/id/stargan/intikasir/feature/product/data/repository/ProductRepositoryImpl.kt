package id.stargan.intikasir.feature.product.data.repository

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
                val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
                entity.toDomain(category?.name)
            }
        }
    }

    override fun getProductById(productId: String): Flow<Product?> {
        return productDao.getProductByIdFlow(productId).map { entity ->
            entity?.let {
                val category = it.categoryId?.let { id -> categoryDao.getCategoryById(id) }
                it.toDomain(category?.name)
            }
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId).map { entities ->
            val category = categoryDao.getCategoryById(categoryId)
            entities.map { it.toDomain(category?.name) }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { entity ->
                val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
                entity.toDomain(category?.name)
            }
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { entity ->
                val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
                entity.toDomain(category?.name)
            }
        }
    }

    override suspend fun insertProduct(product: Product) {
        val entity = product.copy(
            id = if (product.id.isEmpty()) UUID.randomUUID().toString() else product.id,
            updatedAt = System.currentTimeMillis()
        ).toEntity()
        productDao.insertProduct(entity)
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

