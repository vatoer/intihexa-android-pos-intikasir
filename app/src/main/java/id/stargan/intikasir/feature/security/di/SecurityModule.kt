package id.stargan.intikasir.feature.security.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.feature.security.data.RolePermissionRepositoryImpl
import id.stargan.intikasir.feature.security.data.RolePermissionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindRolePermissionRepository(
        impl: RolePermissionRepositoryImpl
    ): RolePermissionRepository
}

