package id.stargan.intikasir.data.local.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import id.stargan.intikasir.data.local.entity.CategoryEntity
import id.stargan.intikasir.data.local.entity.ProductEntity
import id.stargan.intikasir.data.local.entity.UserEntity
import id.stargan.intikasir.data.local.entity.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
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
        scope.launch(Dispatchers.IO) { populateSampleData(db, initialCreate = true) }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Re-seed categories/products if user cleared them manually (idempotent)
        scope.launch(Dispatchers.IO) { populateSampleData(db, initialCreate = false) }
    }

    private fun populateSampleData(db: SupportSQLiteDatabase, initialCreate: Boolean) {
        val timestamp = System.currentTimeMillis()

        // Check existing counts to avoid duplicate inserts
        fun tableCount(table: String): Long {
            // store_settings table doesn't have isDeleted column (single row table)
            val query = if (table == "store_settings") {
                "SELECT COUNT(*) FROM $table"
            } else {
                "SELECT COUNT(*) FROM $table WHERE isDeleted = 0"
            }
            db.query(query).use { cursor ->
                return if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        }

        // Only insert users on first create (never duplicate)
        if (initialCreate) {
            // Helper function to hash PIN
            fun hashPin(pin: String): String {
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(pin.toByteArray())
                return hashBytes.joinToString("") { "%02x".format(it) }
            }
            val adminPin = hashPin("1111")
            val cashierPin = hashPin("2222")
            db.execSQL(
                """
                INSERT INTO users (id, name, pin, role, isActive, createdAt, updatedAt, isDeleted)
                VALUES (?, ?, ?, ?, 1, ?, ?, 0)
                """.trimIndent(),
                arrayOf(UUID.randomUUID().toString(), "Admin", adminPin, "ADMIN", timestamp, timestamp)
            )
            db.execSQL(
                """
                INSERT INTO users (id, name, pin, role, isActive, createdAt, updatedAt, isDeleted)
                VALUES (?, ?, ?, ?, 1, ?, ?, 0)
                """.trimIndent(),
                arrayOf(UUID.randomUUID().toString(), "Kasir", cashierPin, "CASHIER", timestamp, timestamp)
            )
        }

        val categoryCount = tableCount("categories")
        val productCount = tableCount("products")
        val settingsCount = tableCount("store_settings")

        // Seed default store settings if missing (acts as seedingComplete flag)
        if (settingsCount == 0L) {
            db.execSQL(
                """
                INSERT INTO store_settings (
                    id, storeName, storeAddress, storePhone, storeEmail, storeLogo,
                    taxEnabled, taxPercentage, taxName, serviceEnabled, servicePercentage, serviceName,
                    receiptHeader, receiptFooter, printLogo,
                    printerName, printerAddress, printerConnected, printFormat, autoCut, useEscPosDirect,
                    currencySymbol, currencyCode, paperWidthMm, paperCharPerLine,
                    createdAt, updatedAt, syncedAt
                ) VALUES (
                    'store_settings', '', '', '', NULL, NULL,
                    0, 0.0, 'PPN', 0, 0.0, 'Servis',
                    NULL, NULL, 0,
                    NULL, NULL, 0, 'THERMAL', 1, 0,
                    'Rp', 'IDR', 58, 32,
                    ?, ?, NULL
                )
                """.trimIndent(),
                arrayOf(timestamp, timestamp)
            )
        }

        if (categoryCount > 0 && productCount > 0) return // Already seeded

        // 5 Kategori Sample (only if empty)
        if (categoryCount == 0L) {
            val categories = listOf(
                CategoryEntity(UUID.randomUUID().toString(), "Makanan", "Produk makanan dan cemilan", "#FF6B6B", null, 0, true, false, timestamp, timestamp),
                CategoryEntity(UUID.randomUUID().toString(), "Minuman", "Minuman segar dan kemasan", "#4ECDC4", null, 0, true, false, timestamp, timestamp),
                CategoryEntity(UUID.randomUUID().toString(), "Elektronik", "Peralatan elektronik", "#45B7D1", null, 0, true, false, timestamp, timestamp),
                CategoryEntity(UUID.randomUUID().toString(), "Alat Tulis", "Perlengkapan tulis dan kantor", "#96CEB4", null, 0, true, false, timestamp, timestamp),
                CategoryEntity(UUID.randomUUID().toString(), "Kebutuhan Rumah", "Produk kebutuhan sehari-hari", "#FFEAA7", null, 0, true, false, timestamp, timestamp)
            )
            categories.forEach { category ->
                db.execSQL(
                    """
                    INSERT INTO categories (id, name, description, color, icon, 'order', isActive, createdAt, updatedAt, isDeleted)
                    VALUES (?, ?, ?, ?, ?, 0, 1, ?, ?, 0)
                    """.trimIndent(),
                    arrayOf(category.id, category.name, category.description, category.color, category.icon, category.createdAt, category.updatedAt)
                )
            }
        }

        // 10 Produk Sample (only if empty)
        if (productCount == 0L) {
            // Fetch category IDs just inserted or existing
            val categoryIds = mutableListOf<String>()
            db.query("SELECT id FROM categories WHERE isDeleted = 0 ORDER BY name ASC").use { cursor ->
                while (cursor.moveToNext()) categoryIds += cursor.getString(0)
            }
            if (categoryIds.size < 5) return // safety
            val products = listOf(
                ProductEntity(UUID.randomUUID().toString(), "Nasi Goreng", "Nasi goreng spesial dengan telur", 15000.0, 10000.0, 50, 10, 10, categoryIds[0], "MKN-001", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Mie Ayam", "Mie ayam dengan bakso", 12000.0, 8000.0, 40, 10, 10, categoryIds[0], "MKN-002", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Es Teh Manis", "Teh manis dingin segar", 5000.0, 2000.0, 100, 20, 20, categoryIds[1], "MNM-001", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Kopi Susu", "Kopi susu premium", 8000.0, 4000.0, 80, 15, 15, categoryIds[1], "MNM-002", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Kabel USB Type-C", "Kabel USB Type-C 1 meter", 25000.0, 15000.0, 30, 5, 5, categoryIds[2], "ELK-001", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Earphone", "Earphone dengan microphone", 35000.0, 20000.0, 25, 5, 5, categoryIds[2], "ELK-002", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Pulpen", "Pulpen tinta hitam", 3000.0, 1500.0, 100, 20, 20, categoryIds[3], "ATK-001", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Buku Tulis", "Buku tulis 38 lembar", 5000.0, 3000.0, 60, 15, 15, categoryIds[3], "ATK-002", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Sabun Cuci Piring", "Sabun cuci piring 800ml", 12000.0, 8000.0, 35, 10, 10, categoryIds[4], "RMH-001", null, null, true, false, timestamp, timestamp),
                ProductEntity(UUID.randomUUID().toString(), "Tisu Wajah", "Tisu wajah isi 250 lembar", 8000.0, 5000.0, 50, 10, 10, categoryIds[4], "RMH-002", null, null, true, false, timestamp, timestamp)
            )
            products.forEach { product ->
                db.execSQL(
                    """
                    INSERT INTO products (
                        id, name, description, price, cost, stock, minStock, lowStockThreshold, categoryId, sku, barcode, imageUrl,
                        isActive, createdAt, updatedAt, isDeleted
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, 0)
                    """.trimIndent(),
                    arrayOf(
                        product.id,
                        product.name,
                        product.description,
                        product.price,
                        product.cost,
                        product.stock,
                        product.minStock,
                        product.lowStockThreshold,
                        product.categoryId,
                        product.sku,
                        product.barcode,
                        product.imageUrl,
                        product.createdAt,
                        product.updatedAt
                    )
                )
            }
        }
    }
}
