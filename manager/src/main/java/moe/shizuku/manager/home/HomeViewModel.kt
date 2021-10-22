package moe.shizuku.manager.home

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.Manifest
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.utils.Logger.LOGGER
import moe.shizuku.manager.utils.ShizukuSystemApis
import rikka.lifecycle.Resource
import rikka.shizuku.Shizuku

class HomeViewModel : ViewModel() {

    private val _serviceStatus = MutableLiveData<Resource<ServiceStatus>>()
    val serviceStatus = _serviceStatus as LiveData<Resource<ServiceStatus>>

    private fun load(): ServiceStatus {
        if (!Shizuku.pingBinder()) {
            return ServiceStatus()
        }

        val uid = Shizuku.getUid()
        val apiVersion = Shizuku.getVersion()
        val patchVersion = Shizuku.getServerPatchVersion().let { if (it < 0) 0 else it }
        val seContext = if (apiVersion >= 6) {
            try {
                Shizuku.getSELinuxContext()
            } catch (tr: Throwable) {
                LOGGER.w(tr, "getSELinuxContext")
                null
            }
        } else null
        val permissionTest =
            Shizuku.checkRemotePermission("android.permission.GRANT_RUNTIME_PERMISSIONS") == PackageManager.PERMISSION_GRANTED

        // Before a526d6bb, server will not exit on uninstall, manager installed later will get not permission
        // Run a random remote transaction here, report no permission as not running
        ShizukuSystemApis.checkPermission(Manifest.permission.API_V23, BuildConfig.APPLICATION_ID, 0)
        return ServiceStatus(uid, apiVersion, patchVersion, seContext, permissionTest)
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = load()
                _serviceStatus.postValue(Resource.success(status))
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _serviceStatus.postValue(Resource.error(e, ServiceStatus()))
            }
        }
    }
}
