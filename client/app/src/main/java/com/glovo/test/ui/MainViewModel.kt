package com.glovo.test.ui

import androidx.lifecycle.ViewModel
import com.glovo.test.data.repositories.CitiesRepository
import com.glovo.test.data.repositories.CountriesRepository
import com.glovo.test.di.modules.Callback
import com.glovo.test.di.modules.IoThreads
import io.reactivex.Scheduler
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val citiesRepository: CitiesRepository,
    val countriesRepository: CountriesRepository,
    @IoThreads val ioScheduler: Scheduler,
    @Callback val callbackScheduler: Scheduler
) : ViewModel() {


}