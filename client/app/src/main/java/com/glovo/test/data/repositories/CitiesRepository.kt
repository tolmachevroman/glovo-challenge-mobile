package com.glovo.test.data.repositories

import com.glovo.test.data.models.City
import com.glovo.test.di.interactors.WebService
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class CitiesRepository @Inject constructor(private val webService: WebService) {

    fun getCities(): Single<List<City>> {
        return webService.getCities()
    }
}