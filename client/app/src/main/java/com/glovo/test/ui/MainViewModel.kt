package com.glovo.test.ui

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.R
import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.interactors.Response
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val context: Context,
    private val citiesRepository: CitiesRepository,
    @IoThreads private val ioScheduler: Scheduler,
    @Callback private val callbackScheduler: Scheduler
) : ViewModel() {

    private val overlayColor by lazy {
        ResourcesCompat.getColor(context.resources, R.color.polygonFillColor, null)
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    val citiesResponse: MutableLiveData<Response<CityWithPolygonOptions>> = MutableLiveData()
    val cityByCodeResponse: MutableLiveData<Response<City>> = MutableLiveData()

    fun getCities() {
        citiesRepository.getCities()
            .subscribeOn(ioScheduler)
            .observeOn(ioScheduler)
            .subscribeBy(onSuccess = { cities ->

                Observable.fromIterable(cities)
                    .flatMap {
                        Single.just(buildCityWithPolygonOptions(it))
                            .toObservable()
                    }
                    .subscribeOn(ioScheduler)
                    .observeOn(callbackScheduler)
                    .subscribeBy(onNext = { cityWithPolygonOptions ->
                        // we add new cities one by one
                        citiesResponse.value = Response.success(cityWithPolygonOptions)
                    }, onError = {
                        //TODO
                    })

            }, onError = {
                citiesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    fun getCityByCode(code: String) {
        citiesRepository.getCityByCode(code)
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { city ->
                cityByCodeResponse.value = Response.success(city)
            }, onError = {
                citiesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    private fun buildCityWithPolygonOptions(city: City): CityWithPolygonOptions {
        val polygonOptionsAvailable = mutableListOf<PolygonOptions>()
        city.workingArea.forEach { area ->
            val pointsInArea = PolyUtil.decode(area)
            if (pointsInArea.isNotEmpty()) {
                polygonOptionsAvailable.add(
                    PolygonOptions()
                        .strokeWidth(1.0f)
                        .fillColor(overlayColor)
                        .addAll(pointsInArea)
                )
            }
        }
        return CityWithPolygonOptions(
            name = city.name,
            code = city.code,
            polygonOptionsAvailable = polygonOptionsAvailable
        )
    }

    override fun onCleared() {
        disposable.clear()
    }

    data class CityWithPolygonOptions(
        val name: String,
        val code: String,
        val polygonOptionsAvailable: List<PolygonOptions>
    )
}