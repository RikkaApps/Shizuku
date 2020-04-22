package moe.shizuku.manager.utils

import android.os.Build

object BuildUtils {

    @JvmStatic
    fun atLeast24():Boolean {
        return Build.VERSION.SDK_INT >= 24
    }

    @JvmStatic
    fun atLeast29():Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

    @JvmStatic
    fun atLeast30():Boolean {
        return Build.VERSION.SDK_INT >= 30 || (Build.VERSION.SDK_INT == 29 && Build.VERSION.PREVIEW_SDK_INT > 0)
    }
}