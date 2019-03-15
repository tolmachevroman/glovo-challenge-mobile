package com.glovo.test.di.modules

import com.glovo.test.common.rx.AndroidRxSchedulers
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Singleton

@Module
class SchedulersModule {

    @Provides
    @Singleton
    @SingleBgThread
    fun singleScheduler(): Scheduler = AndroidRxSchedulers.single()

    @Provides
    @Singleton
    @ComputationThreads
    fun computationScheduler(): Scheduler = AndroidRxSchedulers.computation()

    @Provides
    @Singleton
    @IoThreads
    fun ioScheduler(): Scheduler = AndroidRxSchedulers.io()

    @Provides
    @Singleton
    @Callback
    fun callbackScheduler(): Scheduler = AndroidSchedulers.mainThread()
}