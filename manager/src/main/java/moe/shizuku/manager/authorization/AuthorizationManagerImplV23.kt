package moe.shizuku.manager.authorization

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.os.RemoteException
import hidden.HiddenApiBridge
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper
import moe.shizuku.manager.Manifest
import moe.shizuku.manager.utils.BuildUtils.atLeastR
import moe.shizuku.manager.utils.ShizukuSystemApis
import java.util.*

class AuthorizationManagerImplV23 : AuthorizationManagerImpl {

    override fun getPackages(pmFlags: Int): List<PackageInfo> {
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

    override fun granted(packageName: String, uid: Int): Boolean {
        return ShizukuSystemApis.checkPermission(Manifest.permission.API_V23, packageName, Process.myUid() / 100000) == PackageManager.PERMISSION_GRANTED
    }

    override fun grant(packageName: String, uid: Int) {
        ShizukuSystemApis.grantRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000)
    }

    override fun revoke(packageName: String, uid: Int) {
        ShizukuSystemApis.revokeRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000)
    }
}