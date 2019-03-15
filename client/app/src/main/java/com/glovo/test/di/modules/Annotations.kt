package com.glovo.test.di.modules

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Callback

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoThreads

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ComputationThreads

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleBgThread