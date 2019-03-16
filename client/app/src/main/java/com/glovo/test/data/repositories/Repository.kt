package com.glovo.test.data.repositories

import io.reactivex.Single

interface Repository<T> {
    fun getItems(): Single<List<T>>
}