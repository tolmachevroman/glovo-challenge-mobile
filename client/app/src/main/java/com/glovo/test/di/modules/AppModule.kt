package com.glovo.test.di.modules

import android.app.Application
import android.content.Context
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.api.WebService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    @Singleton
    fun providesCitiesRepository(webService: WebService): CitiesRepository = CitiesRepository(webService)

    @Provides
    @Singleton
    fun providesCountriesRepository(webService: WebService): CountriesRepository = CountriesRepository(webService)
}