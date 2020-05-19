@file:Suppress("NOTHING_TO_INLINE")

package moe.shizuku.manager.ktx

import android.util.Log

inline val <reified T> T.TAG: String
    get() =
        T::class.java.simpleName.let {
            if (it.isBlank()) throw IllegalStateException("tag is empty")
            if (it.length > 23) it.substring(0, 23) else it
        }

inline fun <reified T> T.logv(message: String, throwable: Throwable? = null) = logv(TAG, message, throwable)
inline fun <reified T> T.logi(message: String, throwable: Throwable? = null) = logi(TAG, message, throwable)
inline fun <reified T> T.logw(message: String, throwable: Throwable? = null) = logw(TAG, message, throwable)
inline fun <reified T> T.logd(message: String, throwable: Throwable? = null) = logd(TAG, message, throwable)
inline fun <reified T> T.loge(message: String, throwable: Throwable? = null) = loge(TAG, message, throwable)

inline fun <reified T> T.logv(tag: String, message: String, throwable: Throwable? = null) = Log.v(tag, message, throwable)
inline fun <reified T> T.logi(tag: String, message: String, throwable: Throwable? = null) = Log.i(tag, message, throwable)
inline fun <reified T> T.logw(tag: String, message: String, throwable: Throwable? = null) = Log.w(tag, message, throwable)
inline fun <reified T> T.logd(tag: String, message: String, throwable: Throwable? = null) = Log.d(tag, message, throwable)
inline fun <reified T> T.loge(tag: String, message: String, throwable: Throwable? = null) = Log.e(tag, message, throwable)