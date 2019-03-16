package com.glovo.test.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.interactors.Response
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val citiesRepository: CitiesRepository,
    private val countriesRepository: CountriesRepository,
    @IoThreads private val ioScheduler: Scheduler,
    @Callback private val callbackScheduler: Scheduler
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val citiesResponse: MutableLiveData<Response<List<City>>> = MutableLiveData()

    val countriesResponse: MutableLiveData<Response<List<Country>>> = MutableLiveData()

    fun getCities() {
        citiesRepository.getCities()
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { cities ->
                citiesResponse.value = Response.success(cities)
            }, onError = {
                citiesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    fun getCountries() {
        countriesRepository.getCountries()
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { countries ->
                countriesResponse.value = Response.success(countries)
            }, onError = {
                countriesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    override fun onCleared() {
        disposable.clear()
    }
}