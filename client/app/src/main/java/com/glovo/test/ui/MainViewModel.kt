package com.glovo.test.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.data.models.City
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.di.interactors.Response
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val citiesRepository: CitiesRepository,
    @IoThreads private val ioScheduler: Scheduler,
    @Callback private val callbackScheduler: Scheduler
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val cityDetailsByCodeResponse: MutableLiveData<Response<City>> = MutableLiveData()
    val workingAreasDecodedResponse: MutableLiveData<Response<List<WorkingAreaDecoded>>> = MutableLiveData()

    fun getCityByCurrentLocation(currentCoordinate: LatLng) {
        citiesRepository.getCities()
            .subscribeOn(ioScheduler)
            .observeOn(ioScheduler)
            .subscribeBy(onSuccess = { cities ->

                Observable.fromIterable(cities)
                    .flatMap { city ->

                        val builder = LatLngBounds.Builder()

                        val workingAreasDecoded = mutableListOf<WorkingAreaDecoded>()
                        city.workingArea.forEach { areaEncoded ->
                            val pointsDecoded = PolyUtil.decode(areaEncoded)
                            workingAreasDecoded.add(WorkingAreaDecoded(pointsDecoded))
                            pointsDecoded.forEach { builder.include(it) }
                        }
                        val bounds: LatLngBounds = builder.build()
                        val containsCurrentCoordinate = bounds.contains(currentCoordinate)

                        Single.just(Triple(workingAreasDecoded, containsCurrentCoordinate, city))
                            .toObservable()
                    }
                    .subscribeOn(ioScheduler)
                    .observeOn(callbackScheduler)
                    .subscribeBy(onNext = { (areas, containsCurrentCoordinate, city) ->
                        // only if city contains my current coordinates
                        if (containsCurrentCoordinate) {
                            workingAreasDecodedResponse.value = Response.success(areas)
                            getCityByCode(city.code, decodeArea = false)
                        }
                    }, onError = {
                        //TODO
                    })

            }, onError = {
                workingAreasDecodedResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    fun getCityByCode(code: String, decodeArea: Boolean) {
        citiesRepository.getCityByCode(code)
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { city ->
                cityDetailsByCodeResponse.value = Response.success(city)
                if (decodeArea) {
                    decodeWorkingArea(city.workingArea)
                }
            }, onError = {
                cityDetailsByCodeResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    private fun decodeWorkingArea(workingArea: List<String>) {
        Single.just(workingArea)
            .map {
                val workingAreasDecoded = mutableListOf<WorkingAreaDecoded>()
                workingArea.forEach { areaEncoded ->
                    workingAreasDecoded.add(WorkingAreaDecoded(PolyUtil.decode(areaEncoded)))
                }
                workingAreasDecoded
            }
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = {
                workingAreasDecodedResponse.value = Response.success(it)
            }, onError = {
                //TODO
            }).addTo(disposable)
    }


    override fun onCleared() {
        disposable.clear()
    }

    data class WorkingAreaDecoded(val points: List<LatLng>)
}