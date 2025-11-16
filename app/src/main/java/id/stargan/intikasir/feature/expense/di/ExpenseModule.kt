package id.stargan.intikasir.feature.expense.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.data.local.dao.ExpenseDao
import id.stargan.intikasir.feature.expense.data.ExpenseRepositoryImpl
import id.stargan.intikasir.feature.expense.domain.ExpenseRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao)
    }
}

