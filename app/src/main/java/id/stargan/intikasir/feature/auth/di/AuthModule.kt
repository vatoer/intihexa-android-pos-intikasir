package id.stargan.intikasir.feature.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.feature.auth.data.repository.AuthRepositoryImpl
import id.stargan.intikasir.feature.auth.domain.repository.AuthRepository
import javax.inject.Singleton

/**
 * Hilt module untuk authentication feature
 * Provides repository implementation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}

