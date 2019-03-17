package com.glovo.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.glovo.test.data.models.City
import com.glovo.test.data.models.Country
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.ui.adapters.CitiesByCountryAdapter
import com.glovo.test.ui.viewmodels.SelectCityViewModel
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class SelectCityViewModelTest {

    @Rule
    @JvmField
    var instantExecutor = InstantTaskExecutorRule()

    @Mock
    lateinit var citiesRepository: CitiesRepository

    @Mock
    lateinit var countriesRepository: CountriesRepository

    lateinit var selectCityViewModel: SelectCityViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        selectCityViewModel = SelectCityViewModel(citiesRepository, countriesRepository, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun buildAdapterItemsListTest() {
        val adapterItems = selectCityViewModel.buildAdapterItemsList(citiesMockData, countriesMockData)

        assert((adapterItems[0] as CitiesByCountryAdapter.CountryItem).name == "Spain")
        assert((adapterItems[1] as CitiesByCountryAdapter.CityItem).name == "Bilbao")
        assert((adapterItems[2] as CitiesByCountryAdapter.CityItem).name == "Las Palmas de Gran Canaria")
        assert((adapterItems[3] as CitiesByCountryAdapter.CountryItem).name == "Portugal")
        assert((adapterItems[4] as CitiesByCountryAdapter.CityItem).name == "Lisboa")
    }

    private val citiesMockData by lazy {

        val cities: MutableList<City> = mutableListOf()
        cities.add(City(code = "BIL", name = "Bilbao", countryCode = "ES", workingArea = listOf()))
        cities.add(City(code = "LIS", name = "Lisboa", countryCode = "PT", workingArea = listOf()))
        cities.add(City(code = "LPA", name = "Las Palmas de Gran Canaria", countryCode = "ES", workingArea = listOf()))

        cities
    }

    private val countriesMockData by lazy {

        val countries: MutableList<Country> = mutableListOf()
        countries.add(Country(code = "ES", name = "Spain"))
        countries.add(Country(code = "PT", name = "Portugal"))

        countries
    }
}