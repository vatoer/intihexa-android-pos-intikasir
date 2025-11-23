package id.stargan.intikasir.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import id.stargan.intikasir.data.local.dao.UserDao
import id.stargan.intikasir.data.local.dao.CategoryDao
import id.stargan.intikasir.data.local.dao.ProductDao
import id.stargan.intikasir.data.local.dao.TransactionDao
import id.stargan.intikasir.data.local.dao.TransactionItemDao
import id.stargan.intikasir.data.local.dao.StoreSettingsDao
import id.stargan.intikasir.data.local.dao.ExpenseDao
import id.stargan.intikasir.data.local.dao.RolePermissionDao
import id.stargan.intikasir.data.local.entity.UserEntity
import id.stargan.intikasir.data.local.entity.CategoryEntity
import id.stargan.intikasir.data.local.entity.ProductEntity
import id.stargan.intikasir.data.local.entity.TransactionEntity
import id.stargan.intikasir.data.local.entity.TransactionItemEntity
import id.stargan.intikasir.data.local.entity.StoreSettingsEntity
import id.stargan.intikasir.data.local.entity.ExpenseEntity
import id.stargan.intikasir.data.local.entity.RolePermissionEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StoreSettingsEntity::class,
        ExpenseEntity::class,
        RolePermissionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class IntiKasirDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun storeSettingsDao(): StoreSettingsDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun rolePermissionDao(): RolePermissionDao

    companion object {
        const val DATABASE_NAME = "intikasir_database"
    }
}
