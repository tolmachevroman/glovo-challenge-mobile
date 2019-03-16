package com.glovo.test.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.interactors.Response
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import javax.inject.Inject

class SelectCityViewModel @Inject constructor(
    private val citiesRepository: CitiesRepository,
    private val countriesRepository: CountriesRepository,
    @IoThreads private val ioScheduler: Scheduler,
    @Callback private val callbackScheduler: Scheduler
) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    val citiesGroupedByCountriesResponse: MutableLiveData<Response<List<CitiesByCountryAdapter.AdapterItem>>> = MutableLiveData()

    fun getCitiesGroupedByCountries() {
        citiesGroupedByCountriesResponse.value = Response.loading(null)
        citiesRepository.getCities()
            .zipWith(countriesRepository.getCountries())
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { (cities, countries) ->

                val adapterItems = mutableListOf<CitiesByCountryAdapter.AdapterItem>()

                countries.forEach { country ->
                    adapterItems.add(CitiesByCountryAdapter.CountryItem(country.name))
                    cities.filter { city ->
                        city.countryCode == country.code
                    }.forEach { city ->
                        adapterItems.add(CitiesByCountryAdapter.CityItem(city.name, city.code))
                    }
                }

                citiesGroupedByCountriesResponse.value = Response.success(adapterItems)
            }, onError = {
                citiesGroupedByCountriesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }
}