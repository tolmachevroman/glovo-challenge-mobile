package com.glovo.test.data.repositories

import com.glovo.test.data.models.Country
import com.glovo.test.di.interactors.WebService
import io.reactivex.Single
import javax.inject.Inject

class CountriesRepository @Inject constructor(private val webService: WebService) : Repository<Country> {

    override fun getItems(): Single<List<Country>> {
        return webService.getCountries()
    }
}