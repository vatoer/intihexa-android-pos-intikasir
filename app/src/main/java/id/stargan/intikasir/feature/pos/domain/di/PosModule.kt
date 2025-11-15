package id.stargan.intikasir.feature.pos.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.feature.pos.domain.TransactionRepository
import id.stargan.intikasir.feature.pos.data.TransactionRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PosModule {
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
}

