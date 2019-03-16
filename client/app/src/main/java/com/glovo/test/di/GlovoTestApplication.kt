package com.glovo.test.di

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.glovo.test.BuildConfig
import com.glovo.test.di.components.DaggerAppComponent
import com.glovo.test.di.modules.AppModule
import com.glovo.test.di.modules.NetworkModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class GlovoTestApplication : Application(), HasActivityInjector, HasSupportFragmentInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

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

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}