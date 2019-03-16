package com.glovo.test.common.rx

import android.util.Log
import androidx.lifecycle.Observer
import com.glovo.test.di.interactors.Response

class ResponseObserver<T>(
    private val tag: String,
    private val hideLoading: (() -> Unit)? = null,
    private val showLoading: (() -> Unit)? = null,
    private val onSuccess: ((data: T) -> Unit)? = null,
    private val onError: ((error: Throwable?) -> Unit)? = null
) : Observer<Response<T>> {

    override fun onChanged(response: Response<T>?) {
        when (response?.status) {
            Response.Status.SUCCESS -> {
                hideLoading?.invoke()
                if (response.data != null) {
                    Log.d(tag, "observer -> SUCCESS, ${response.data} items")
                    onSuccess?.invoke(response.data)
                }
            }
            Response.Status.ERROR -> {
                hideLoading?.invoke()
                if (response.error != null) {
                    Log.d(tag, "observer -> ERROR, ${response.error}")
                    onError?.invoke(response.error)
                }
            }
            Response.Status.LOADING -> {
                showLoading?.invoke()
                Log.d(tag, "observer -> LOADING")
            }
        }
    }
}
