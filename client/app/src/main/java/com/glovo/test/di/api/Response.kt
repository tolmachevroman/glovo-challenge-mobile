package com.glovo.test.di.api


class Response<out T> constructor(val status: Status, val data: T?, val error: Throwable?) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {

        fun <T> success(data: T?): Response<T> = Response(Status.SUCCESS, data, null)

        fun <T> error(data: T? = null, error: Throwable?): Response<T> = Response(Status.ERROR, data, error)

        fun <T> loading(data: T?): Response<T> = Response(Status.LOADING, data, null)
    }

    override fun toString(): String =
        status.toString() + ", data:" + data?.toString() + ", error:" + error.toString()
}