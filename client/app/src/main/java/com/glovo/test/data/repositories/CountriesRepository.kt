package com.glovo.test.data.repositories

import com.glovo.test.data.models.Country
import com.glovo.test.di.api.WebService
import io.reactivex.Single
import javax.inject.Inject

class CountriesRepository @Inject constructor(private val webService: WebService) {

    fun getCountries(): Single<List<Country>> {
        return webService.getCountries()
    }
}