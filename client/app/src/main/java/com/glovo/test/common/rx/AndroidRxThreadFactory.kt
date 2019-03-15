package com.glovo.test.common.rx

import android.os.Process
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class AndroidRxThreadFactory(private val prefix: String, private val androidThreadPriority: Int) : ThreadFactory {

    private val counter = AtomicInteger()

    override fun newThread(runnable: Runnable?) = Thread(
        {
            Process.setThreadPriority(androidThreadPriority)
            runnable?.run()
        },
        "$prefix ${counter.incrementAndGet()}")
        .apply { isDaemon = true }
}