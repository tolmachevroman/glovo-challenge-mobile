package com.glovo.test.di.interactors

import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import io.reactivex.Single
import retrofit2.http.GET

interface WebService {

    @GET("cities")
    fun getCities(): Single<List<City>>

    @GET("countries")
    fun getCountries(): Single<List<Country>>
}