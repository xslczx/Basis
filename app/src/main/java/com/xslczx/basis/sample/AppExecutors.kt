package com.xslczx.basis.sample

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AppExecutors {
    val io: ExecutorService by lazy {
        Executors.newCachedThreadPool()
    }

    val compute: ExecutorService by lazy {
        val threadCount = 3
        Executors.newFixedThreadPool(threadCount)
    }

    val single: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    val main: Executor by lazy {
        MainThreadExecutor()
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}