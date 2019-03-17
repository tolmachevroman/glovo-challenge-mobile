package com.glovo.test.di.api

import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface WebService {

    @GET("cities")
    fun getCities(): Single<List<City>>

    @GET("cities/{city_code}")
    fun getCityByCode(@Path("city_code") cityCode: String): Single<City>

    @GET("countries")
    fun getCountries(): Single<List<Country>>
}