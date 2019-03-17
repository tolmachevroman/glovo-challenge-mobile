package com.glovo.test.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.data.models.City
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.di.api.Response
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

    /**
     * Observable of city details
     */
    val cityDetailsByCodeResponse: MutableLiveData<Response<City>> = MutableLiveData()

    /**
     * Observable of decoded working areas in a given city
     * City contains several working areas, each of which contains many points to build polygons
     */
    val workingAreasDecodedResponse: MutableLiveData<Response<List<WorkingAreaDecoded>>> = MutableLiveData()

    /**
     * Observable of city markers, returns cached markers list at the moment
     */
    val cityMarkersResponse: MutableLiveData<Response<List<CityMarker>>> = MutableLiveData()

    /**
     * Cached city markers list, to avoid extra expensive center coordinates calculation
     */
    private val cityMarkers: MutableList<CityMarker> = mutableListOf()

    /**
     * Given current user coordinate, traverses through cities list and decodes it's working areas
     * to see if they include the coordinate; if found, retrieves city details and shows it on the map
     */
    fun getCityByCurrentLocation(currentCoordinate: LatLng) {
        workingAreasDecodedResponse.value = Response.loading(data = null)
        citiesRepository.getCities()
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { cities ->

                Observable.fromIterable(cities)
                    .flatMap { city ->

                        val builder = LatLngBounds.Builder()

                        val workingAreasDecoded = mutableListOf<WorkingAreaDecoded>()
                        city.workingArea.forEach { areaEncoded ->
                            val pointsDecoded = PolyUtil.decode(areaEncoded)
                            workingAreasDecoded.add(
                                WorkingAreaDecoded(
                                    pointsDecoded
                                )
                            )
                            pointsDecoded.forEach { builder.include(it) }
                        }
                        val bounds: LatLngBounds = builder.build()
                        val containsCurrentCoordinate = bounds.contains(currentCoordinate)

                        // cache city center for markers
                        cityMarkers.add(
                            CityMarker(
                                code = city.code,
                                center = bounds.center
                            )
                        )

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
                        workingAreasDecodedResponse.value = Response.error(data = null, error = it)
                    }).addTo(disposable)

            }, onError = {
                workingAreasDecodedResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    /**
     * Retrieves city details by city code, and optionally calculates its working areas
     */
    fun getCityByCode(code: String, decodeArea: Boolean) {
        cityDetailsByCodeResponse.value = Response.loading(data = null)
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

    /**
     * Calculates decoded points from working area
     */
    private fun decodeWorkingArea(workingArea: List<String>) {
        workingAreasDecodedResponse.value = Response.loading(data = null)
        Single.just(workingArea)
            .map {
                val workingAreasDecoded = mutableListOf<WorkingAreaDecoded>()
                workingArea.forEach { areaEncoded ->
                    workingAreasDecoded.add(
                        WorkingAreaDecoded(
                            PolyUtil.decode(
                                areaEncoded
                            )
                        )
                    )
                }
                workingAreasDecoded
            }
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = {
                workingAreasDecodedResponse.value = Response.success(it)
            }, onError = {
                workingAreasDecodedResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    fun getCityMarkers() {
        cityMarkersResponse.value = Response.success(cityMarkers)
    }

    override fun onCleared() {
        disposable.clear()
    }

    data class WorkingAreaDecoded(val points: List<LatLng>)
    data class CityMarker(val code: String, val center: LatLng)
}