package com.glovo.test.di

import android.app.Activity
import android.app.Application
import com.glovo.test.BuildConfig
import com.glovo.test.di.components.DaggerAppComponent
import com.glovo.test.di.modules.AppModule
import com.glovo.test.di.modules.NetworkModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class GlovoTestApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .networkModule(NetworkModule(BuildConfig.URL))
            .build()
            .inject(this)

    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
}