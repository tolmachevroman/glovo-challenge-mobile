package com.glovo.test.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.api.Response
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import com.glovo.test.ui.adapters.CitiesByCountryAdapter
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

    /**
     * Observable of adapter's aux AdapterItem list
     */
    val citiesGroupedByCountriesResponse: MutableLiveData<Response<List<CitiesByCountryAdapter.AdapterItem>>> =
        MutableLiveData()

    /**
     * Retrieves cities and countries and merges them in a grouped list
     */
    fun getCitiesGroupedByCountries() {
        citiesGroupedByCountriesResponse.value = Response.loading(null)
        citiesRepository.getCities()
            .zipWith(countriesRepository.getCountries())
            .map { (cities, countries) ->
                buildAdapterItemsList(cities, countries)
            }
            .subscribeOn(ioScheduler)
            .observeOn(callbackScheduler)
            .subscribeBy(onSuccess = { adapterItems ->
                citiesGroupedByCountriesResponse.value = Response.success(adapterItems)
            }, onError = {
                citiesGroupedByCountriesResponse.value = Response.error(data = null, error = it)
            }).addTo(disposable)
    }

    fun buildAdapterItemsList(cities: List<City>, countries: List<Country>): List<CitiesByCountryAdapter.AdapterItem> {
        val adapterItems = mutableListOf<CitiesByCountryAdapter.AdapterItem>()

        countries.forEach { country ->
            adapterItems.add(CitiesByCountryAdapter.CountryItem(country.name))
            cities.filter { city ->
                city.countryCode == country.code
            }.forEach { city ->
                adapterItems.add(CitiesByCountryAdapter.CityItem(city.name, city.code))
            }
        }
        return adapterItems
    }

    override fun onCleared() {
        disposable.clear()
    }
}