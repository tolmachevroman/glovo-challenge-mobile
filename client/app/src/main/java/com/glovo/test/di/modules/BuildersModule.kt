package com.glovo.test.di.modules

import com.glovo.test.ui.activities.MainActivity
import com.glovo.test.ui.fragments.SelectCityFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSelectCityFragment(): SelectCityFragment
}