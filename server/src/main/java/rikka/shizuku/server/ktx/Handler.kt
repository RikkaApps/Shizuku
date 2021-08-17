package rikka.shizuku.server.ktx

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

val mainHandler by lazy {
    Handler(Looper.getMainLooper())
}

private val workerThread by lazy(LazyThreadSafetyMode.NONE) {
    HandlerThread("Worker").apply { start() }
}

val workerHandler by lazy {
    Handler(workerThread.looper)
}
