package id.stargan.intikasir.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.data.local.dao.*
import id.stargan.intikasir.data.local.database.DatabaseCallback
import id.stargan.intikasir.data.local.database.IntiKasirDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IntiKasirDatabase {
        return Room.databaseBuilder(
            context,
            IntiKasirDatabase::class.java,
            IntiKasirDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(DatabaseCallback(CoroutineScope(SupervisorJob())))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: IntiKasirDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: IntiKasirDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: IntiKasirDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: IntiKasirDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionItemDao(database: IntiKasirDatabase): TransactionItemDao {
        return database.transactionItemDao()
    }

    @Provides
    @Singleton
    fun provideStoreSettingsDao(database: IntiKasirDatabase): StoreSettingsDao {
        return database.storeSettingsDao()
    }
}
