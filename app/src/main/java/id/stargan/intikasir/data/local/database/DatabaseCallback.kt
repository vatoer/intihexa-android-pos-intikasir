package id.stargan.intikasir.data.local.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import id.stargan.intikasir.data.local.entity.CategoryEntity
import id.stargan.intikasir.data.local.entity.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Callback untuk populate sample data saat pertama kali database dibuat
 */
class DatabaseCallback(
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Populate sample data di background thread
        scope.launch(Dispatchers.IO) {
            populateSampleData(db)
        }
    }

    private fun populateSampleData(db: SupportSQLiteDatabase) {
        val timestamp = System.currentTimeMillis()

        // 5 Kategori Sample
        val categories = listOf(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = "Makanan",
                description = "Produk makanan dan cemilan",
                color = "#FF6B6B",
                icon = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = "Minuman",
                description = "Minuman segar dan kemasan",
                color = "#4ECDC4",
                icon = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = "Elektronik",
                description = "Peralatan elektronik",
                color = "#45B7D1",
                icon = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = "Alat Tulis",
                description = "Perlengkapan tulis dan kantor",
                color = "#96CEB4",
                icon = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = "Kebutuhan Rumah",
                description = "Produk kebutuhan sehari-hari",
                color = "#FFEAA7",
                icon = null,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )

        // Insert categories
        categories.forEach { category ->
            db.execSQL(
                """
                INSERT INTO categories (id, name, description, color, icon, 'order', isActive, createdAt, updatedAt, isDeleted)
                VALUES (?, ?, ?, ?, ?, 0, 1, ?, ?, 0)
                """.trimIndent(),
                arrayOf(
                    category.id,
                    category.name,
                    category.description,
                    category.color,
                    category.icon,
                    category.createdAt,
                    category.updatedAt
                )
            )
        }

        // 10 Produk Sample
        val products = listOf(
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[0].id,
                name = "Nasi Goreng",
                description = "Nasi goreng spesial dengan telur",
                sku = "MKN-001",
                barcode = null,
                price = 15000.0,
                cost = 10000.0,
                stock = 50,
                minStock = 10,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[0].id,
                name = "Mie Ayam",
                description = "Mie ayam dengan bakso",
                sku = "MKN-002",
                barcode = null,
                price = 12000.0,
                cost = 8000.0,
                stock = 40,
                minStock = 10,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[1].id,
                name = "Es Teh Manis",
                description = "Teh manis dingin segar",
                sku = "MNM-001",
                barcode = null,
                price = 5000.0,
                cost = 2000.0,
                stock = 100,
                minStock = 20,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[1].id,
                name = "Kopi Susu",
                description = "Kopi susu premium",
                sku = "MNM-002",
                barcode = null,
                price = 8000.0,
                cost = 4000.0,
                stock = 80,
                minStock = 15,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[2].id,
                name = "Kabel USB Type-C",
                description = "Kabel USB Type-C 1 meter",
                sku = "ELK-001",
                barcode = null,
                price = 25000.0,
                cost = 15000.0,
                stock = 30,
                minStock = 5,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[2].id,
                name = "Earphone",
                description = "Earphone dengan microphone",
                sku = "ELK-002",
                barcode = null,
                price = 35000.0,
                cost = 20000.0,
                stock = 25,
                minStock = 5,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[3].id,
                name = "Pulpen",
                description = "Pulpen tinta hitam",
                sku = "ATK-001",
                barcode = null,
                price = 3000.0,
                cost = 1500.0,
                stock = 100,
                minStock = 20,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[3].id,
                name = "Buku Tulis",
                description = "Buku tulis 38 lembar",
                sku = "ATK-002",
                barcode = null,
                price = 5000.0,
                cost = 3000.0,
                stock = 60,
                minStock = 15,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[4].id,
                name = "Sabun Cuci Piring",
                description = "Sabun cuci piring 800ml",
                sku = "RMH-001",
                barcode = null,
                price = 12000.0,
                cost = 8000.0,
                stock = 35,
                minStock = 10,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categories[4].id,
                name = "Tisu Wajah",
                description = "Tisu wajah isi 250 lembar",
                sku = "RMH-002",
                barcode = null,
                price = 8000.0,
                cost = 5000.0,
                stock = 50,
                minStock = 10,
                imageUrl = null,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )

        // Insert products
        products.forEach { product ->
            db.execSQL(
                """
                INSERT INTO products (
                    id, categoryId, name, description, sku, barcode, price, cost, stock, minStock, 
                    lowStockThreshold, imageUrl, isActive, createdAt, updatedAt, isDeleted
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, 0)
                """.trimIndent(),
                arrayOf(
                    product.id,
                    product.categoryId,
                    product.name,
                    product.description,
                    product.sku,
                    product.barcode,
                    product.price,
                    product.cost,
                    product.stock,
                    product.minStock,
                    product.minStock, // lowStockThreshold = minStock
                    product.imageUrl,
                    product.createdAt,
                    product.updatedAt
                )
            )
        }
    }
}
