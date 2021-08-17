package rikka.shizuku.server

import android.os.FileObserver
import android.util.Log
import java.io.File
import java.util.*
import kotlin.collections.HashMap

object ApkChangedObservers {

    private val cache = Collections.synchronizedMap(HashMap<String, ApkChangedObserver>())

    private fun findRoot(apkPath: String): String {
        if (!apkPath.startsWith("/data/app/")) return apkPath

        var file: File? = File(apkPath)
        while (file != null && file.parent != "/data/app") {
            file = file.parentFile
        }
        return file?.toString() ?: apkPath
    }

    @JvmStatic
    fun start(apkPath: String, listener: ApkChangedListener): ApkChangedObserver {
        return cache.getOrPut(apkPath) {
            if (File(apkPath).parent == "/data/app") {
                ApkChangedObserver(apkPath, FileObserver.MODIFY or FileObserver.CLOSE_WRITE or FileObserver.DELETE_SELF or FileObserver.MOVE_SELF)
            } else {
                ApkChangedObserver(findRoot(apkPath), FileObserver.CREATE or FileObserver.CLOSE_WRITE or FileObserver.DELETE or FileObserver.DELETE_SELF or FileObserver.MOVED_TO or FileObserver.MOVED_FROM)
            }
        }.apply {
            addListener(listener)
            startWatching()
        }
    }

    @JvmStatic
    fun stop(observer: ApkChangedObserver) {
        observer.stopWatching()
        cache.values.remove(observer)
    }
}

interface ApkChangedListener {
    fun onApkChanged()
}

class ApkChangedObserver internal constructor(private val path: String, mask: Int)
    : FileObserver(path, mask) {

    private val listeners = mutableSetOf<ApkChangedListener>()

    fun addListener(listener: ApkChangedListener) {
        listeners.add(listener)
    }

    override fun onEvent(event: Int, path: String?) {
        Log.d("ShizukuServer", "onEvent: $event $path")
        if ((event and 0x00008000) != 0) return
        stopWatching()
        listeners.forEach { it.onApkChanged() }
    }

    override fun startWatching() {
        super.startWatching()
        Log.d("ShizukuServer", "start watching $path")
    }

    override fun stopWatching() {
        super.stopWatching()
        Log.d("ShizukuServer", "stop watching $path")
    }
}
