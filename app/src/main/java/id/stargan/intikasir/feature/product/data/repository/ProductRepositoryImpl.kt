package id.stargan.intikasir.feature.product.data.repository

import id.stargan.intikasir.data.local.dao.CategoryDao
import id.stargan.intikasir.data.local.dao.ProductDao
import id.stargan.intikasir.data.local.entity.CategoryEntity
import id.stargan.intikasir.data.local.entity.ProductEntity
import id.stargan.intikasir.domain.model.Category
import id.stargan.intikasir.domain.model.Product
import id.stargan.intikasir.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProductRepository
 * Handles product dan category data operations
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryId ->
                    categoryDao.getCategoryById(categoryId)?.name
                }
                entity.toDomainModel(categoryName)
            }
        }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId).map { entities ->
            entities.map { entity ->
                val categoryName = categoryDao.getCategoryById(categoryId)?.name
                entity.toDomainModel(categoryName)
            }
        }
    }

    override fun getProductById(productId: String): Flow<Product?> {
        return flow {
            val entity = productDao.getProductById(productId)
            if (entity != null) {
                val categoryName = entity.categoryId?.let { categoryId ->
                    categoryDao.getCategoryById(categoryId)?.name
                }
                emit(entity.toDomainModel(categoryName))
            } else {
                emit(null)
            }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId?.let { categoryId ->
                    categoryDao.getCategoryById(categoryId)?.name
                }
                entity.toDomainModel(categoryName)
            }
        }
    }

    override suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.softDeleteProduct(productId)
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.softDeleteCategory(categoryId)
    }
}

/**
 * Extension function untuk convert Product domain model ke entity
 */
private fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        cost = this.cost,
        sku = this.sku,
        barcode = this.barcode,
        imageUrl = this.imageUrl,
        categoryId = this.categoryId,
        trackStock = this.trackStock,
        stock = this.stock,
        lowStockThreshold = this.lowStockThreshold,
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Extension function untuk convert ProductEntity ke domain model
 */
private fun ProductEntity.toDomainModel(categoryName: String? = null): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        cost = this.cost,
        sku = this.sku,
        barcode = this.barcode,
        imageUrl = this.imageUrl,
        categoryId = this.categoryId,
        categoryName = categoryName,
        trackStock = this.trackStock,
        stock = this.stock,
        lowStockThreshold = this.lowStockThreshold,
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Extension function untuk convert Category domain model ke entity
 */
private fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        color = this.color,
        icon = this.icon,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Extension function untuk convert CategoryEntity ke domain model
 */
private fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = this.id,
        name = this.name,
        description = this.description,
        color = this.color,
        icon = this.icon,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

