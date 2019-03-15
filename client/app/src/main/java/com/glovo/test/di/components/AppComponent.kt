package com.glovo.test.di.components

import com.glovo.test.di.GlovoTestApplication
import com.glovo.test.di.modules.*
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        (AndroidInjectionModule::class),
        (BuildersModule::class),
        (ViewModelModule::class),
        (AppModule::class),
        (NetworkModule::class),
        (SchedulersModule::class)
    ]
)
interface AppComponent {
    fun inject(app: GlovoTestApplication)
}