package id.stargan.intikasir.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.stargan.intikasir.data.api.ActivationApiService
import id.stargan.intikasir.data.repository.ActivationRepository
import id.stargan.intikasir.data.security.SecurePreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivationRetrofit

@Module
@InstallIn(SingletonComponent::class)
object ActivationModule {

    // Base URL untuk server aktivasi
    // Ganti dengan URL server yang sebenarnya
//    private const val BASE_URL = "http://192.168.18.93:3000/"
    private const val BASE_URL = "https://appreg.stargan.id/"
    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    @ActivationRetrofit
    fun provideActivationOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @ActivationRetrofit
    fun provideActivationRetrofit(
        @ActivationRetrofit okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideActivationApiService(
        @ActivationRetrofit retrofit: Retrofit
    ): ActivationApiService {
        return retrofit.create(ActivationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideActivationRepository(
        apiService: ActivationApiService,
        securePrefs: SecurePreferences,
        @ApplicationContext context: Context
    ): ActivationRepository {
        return ActivationRepository(apiService, securePrefs, context)
    }
}

