package moe.shizuku.manager.authorization

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import moe.shizuku.manager.Manifest
import moe.shizuku.manager.utils.ShizukuSystemApis
import rikka.shizuku.Shizuku
import java.util.*

object AuthorizationManager {

    private const val FLAG_ALLOWED = 1 shl 1
    private const val FLAG_DENIED = 1 shl 2
    private const val MASK_PERMISSION = FLAG_ALLOWED or FLAG_DENIED

    fun getPackages(pmFlags: Int): List<PackageInfo> {
        val packages: MutableList<PackageInfo> = ArrayList()
        for (pi in ShizukuSystemApis.getInstalledPackages(pmFlags or PackageManager.GET_PERMISSIONS, Process.myUid() / 100000)) {
            if (pi.requestedPermissions == null) continue
            for (p in pi.requestedPermissions) {
                if (Manifest.permission.API_V23 == p) {
                    packages.add(pi)
                    break
                }
            }
        }
        return packages
    }

    fun granted(packageName: String, uid: Int): Boolean {
        return (Shizuku.getFlagsForUid(uid, MASK_PERMISSION) and FLAG_ALLOWED) == FLAG_ALLOWED
    }

    fun grant(packageName: String, uid: Int) {
        Shizuku.updateFlagsForUid(uid, MASK_PERMISSION, FLAG_ALLOWED)
    }

    fun revoke(packageName: String, uid: Int) {
        Shizuku.updateFlagsForUid(uid, MASK_PERMISSION, 0)
    }
}