package id.stargan.intikasir.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.data.local.image.ImageRepository
import id.stargan.intikasir.data.local.image.ImageRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {
    @Provides
    @Singleton
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository = ImageRepositoryImpl(context)
}

