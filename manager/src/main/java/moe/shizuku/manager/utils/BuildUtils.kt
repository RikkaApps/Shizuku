package moe.shizuku.manager.utils

import android.os.Build

object BuildUtils {

    @JvmStatic
    fun atLeastQ():Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

    @JvmStatic
    fun atLeastR():Boolean {
        return Build.VERSION.SDK_INT >= 30 || (Build.VERSION.SDK_INT == 29 && Build.VERSION.PREVIEW_SDK_INT > 0)
    }
}