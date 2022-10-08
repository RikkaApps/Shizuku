package rikka.shizuku.server

import android.os.FileObserver
import android.util.Log
import java.io.File
import java.util.*

interface ApkChangedListener {
    fun onApkChanged()
}

private val observers = Collections.synchronizedMap(HashMap<String, ApkChangedObserver>())

object ApkChangedObservers {

    @JvmStatic
    fun start(apkPath: String, listener: ApkChangedListener) {
        // inotify watchs inode, if the there are still processes holds the file, DELTE_SELF will not be triggered
        // so we need to watch the parent folder

        val path = File(apkPath).parent!!
        val observer = observers.getOrPut(path) {
            ApkChangedObserver(path).apply {
                startWatching()
            }
        }
        observer.addListener(listener)
    }

    @JvmStatic
    fun stop(listener: ApkChangedListener) {
        val pathToRemove = mutableListOf<String>()

        for ((path, observer) in observers) {
            observer.removeListener(listener)

            if (!observer.hasListeners()) {
                pathToRemove.add(path)
            }
        }

        for (path in pathToRemove) {
            observers.remove(path)?.stopWatching()
        }
    }
}

class ApkChangedObserver(private val path: String) : FileObserver(path, DELETE) {

    private val listeners = mutableSetOf<ApkChangedListener>()

    fun addListener(listener: ApkChangedListener): Boolean {
        return listeners.add(listener)
    }

    fun removeListener(listener: ApkChangedListener): Boolean {
        return listeners.remove(listener)
    }

    fun hasListeners(): Boolean {
        return listeners.isNotEmpty()
    }

    override fun onEvent(event: Int, path: String?) {
        Log.d("ShizukuServer", "onEvent: ${eventToString(event)} $path")

        if ((event and 0x00008000 /* IN_IGNORED */) != 0 || path == null) {
            return
        }

        if (path == "base.apk") {
            stopWatching()
            ArrayList(listeners).forEach { it.onApkChanged() }
        }
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

private fun eventToString(event: Int): String {
    val sb = StringBuilder()
    if (event and FileObserver.ACCESS == FileObserver.ACCESS) {
        sb.append("ACCESS").append(" | ")
    }
    if (event and FileObserver.MODIFY == FileObserver.MODIFY) {
        sb.append("MODIFY").append(" | ")
    }
    if (event and FileObserver.ATTRIB == FileObserver.ATTRIB) {
        sb.append("ATTRIB").append(" | ")
    }
    if (event and FileObserver.CLOSE_WRITE == FileObserver.CLOSE_WRITE) {
        sb.append("CLOSE_WRITE").append(" | ")
    }
    if (event and FileObserver.CLOSE_NOWRITE == FileObserver.CLOSE_NOWRITE) {
        sb.append("CLOSE_NOWRITE").append(" | ")
    }
    if (event and FileObserver.OPEN == FileObserver.OPEN) {
        sb.append("OPEN").append(" | ")
    }
    if (event and FileObserver.MOVED_FROM == FileObserver.MOVED_FROM) {
        sb.append("MOVED_FROM").append(" | ")
    }
    if (event and FileObserver.MOVED_TO == FileObserver.MOVED_TO) {
        sb.append("MOVED_TO").append(" | ")
    }
    if (event and FileObserver.CREATE == FileObserver.CREATE) {
        sb.append("CREATE").append(" | ")
    }
    if (event and FileObserver.DELETE == FileObserver.DELETE) {
        sb.append("DELETE").append(" | ")
    }
    if (event and FileObserver.DELETE_SELF == FileObserver.DELETE_SELF) {
        sb.append("DELETE_SELF").append(" | ")
    }
    if (event and FileObserver.MOVE_SELF == FileObserver.MOVE_SELF) {
        sb.append("MOVE_SELF").append(" | ")
    }

    if (event and 0x00008000 == 0x00008000) {
        sb.append("IN_IGNORED").append(" | ")
    }

    if (event and 0x40000000 == 0x40000000) {
        sb.append("IN_ISDIR").append(" | ")
    }

    return if (sb.isNotEmpty()) {
        sb.substring(0, sb.length - 3)
    } else {
        sb.toString()
    }
}
