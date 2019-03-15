package com.glovo.test.di.modules

import android.app.Application
import com.glovo.test.BuildConfig
import com.glovo.test.di.interactors.WebService
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule(private val baseUrl: String) {

    @Provides
    @Singleton
    fun provideWebService(retrofit: Retrofit): WebService =
        retrofit.create<WebService>(WebService::class.java)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpCache(app: Application): Cache {
        val cacheSize = 10 * 1024 * 1024L
        return Cache(app.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideOkhttpClient(cache: Cache, interceptor: Interceptor): OkHttpClient {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .cache(cache)
        return client.build()
    }

    @Provides
    @Singleton
    fun provideInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) Level.HEADERS else Level.NONE
        }
    }
}