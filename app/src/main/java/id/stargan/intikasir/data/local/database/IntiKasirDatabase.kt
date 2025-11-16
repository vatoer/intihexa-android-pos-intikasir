package id.stargan.intikasir.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import id.stargan.intikasir.data.local.dao.*
import id.stargan.intikasir.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StoreSettingsEntity::class,
        ExpenseEntity::class
    ],
    version = 2,
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

    companion object {
        const val DATABASE_NAME = "intikasir_database"
    }
}

