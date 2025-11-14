package id.stargan.intikasir.data.local.dao

import androidx.room.*
import id.stargan.intikasir.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductByIdFlow(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isDeleted = 0 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= lowStockThreshold AND stock > 0 AND isDeleted = 0 AND isActive = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = stock - :quantity, updatedAt = :timestamp WHERE id = :productId")
    suspend fun decreaseStock(productId: String, quantity: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock + :quantity, updatedAt = :timestamp WHERE id = :productId")
    suspend fun increaseStock(productId: String, quantity: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isDeleted = 1, updatedAt = :timestamp WHERE id = :productId")
    suspend fun softDeleteProduct(productId: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET syncedAt = :timestamp WHERE id = :productId")
    suspend fun markAsSynced(productId: String, timestamp: Long)

    @Query("SELECT * FROM products WHERE syncedAt IS NULL OR updatedAt > syncedAt")
    suspend fun getUnsyncedProducts(): List<ProductEntity>
}

