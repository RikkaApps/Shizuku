package moe.shizuku.starter.ktx

import android.content.IContentProvider
import android.os.Build
import android.os.Bundle
import android.os.RemoteException

@Throws(RemoteException::class)
fun IContentProvider.callCompat(callingPkg: String?, featureId: String?, authority: String?, method: String?, arg: String?, extras: Bundle?): Bundle {
    return when {
        Build.VERSION.SDK_INT >= 30 -> {
            call(callingPkg, featureId, authority, method, arg, extras)
        }
        Build.VERSION.SDK_INT >= 29 -> {
            call(callingPkg, authority, method, arg, extras)
        }
        else -> {
            call(callingPkg, method, arg, extras)
        }
    }
}