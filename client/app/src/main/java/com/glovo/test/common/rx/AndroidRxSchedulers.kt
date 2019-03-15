package com.glovo.test.common.rx

import android.os.Process
import io.reactivex.Scheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object AndroidRxSchedulers {

    fun single(): Scheduler = RxJavaPlugins.createSingleScheduler(
        AndroidRxThreadFactory("RxSingleScheduler-", Process.THREAD_PRIORITY_BACKGROUND)
    )

    fun io(): Scheduler = Schedulers.from(
        Executors.newCachedThreadPool(
            AndroidRxThreadFactory("RxIoScheduler-", Process.THREAD_PRIORITY_BACKGROUND)
        )
    )

    fun computation(): Scheduler = RxJavaPlugins.createComputationScheduler(
        AndroidRxThreadFactory("RxComputationScheduler-", Process.THREAD_PRIORITY_BACKGROUND)
    )
}