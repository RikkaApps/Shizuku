package moe.shizuku.server.ktx

import android.content.AttributionSource
import android.content.IContentProvider
import android.os.Bundle
import android.os.RemoteException
import moe.shizuku.server.utils.BuildUtils
import moe.shizuku.server.utils.OsUtils

@Throws(RemoteException::class)
fun IContentProvider.callCompat(callingPkg: String?, authority: String?, method: String?, arg: String?, extras: Bundle?): Bundle {
    return when {
        BuildUtils.atLeast31() -> {
            try {
                call(AttributionSource.Builder(OsUtils.getUid()).setPackageName(callingPkg).build(), authority, method, arg, extras)
            } catch (e: Throwable) {
                e.printStackTrace()
                call(callingPkg, null, authority, method, arg, extras)
            }
        }
        BuildUtils.atLeast30() -> {
            call(callingPkg, null, authority, method, arg, extras)
        }
        BuildUtils.atLeast29() -> {
            call(callingPkg, authority, method, arg, extras)
        }
        else -> {
            call(callingPkg, method, arg, extras)
        }
    }
}
