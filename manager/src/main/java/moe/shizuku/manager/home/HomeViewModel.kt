package moe.shizuku.manager.home

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.utils.Logger.LOGGER
import moe.shizuku.manager.viewmodel.Resource
import rikka.shizuku.Shizuku

class HomeViewModel : ViewModel() {

    private val _serviceStatus = MutableLiveData<Resource<ServiceStatus>>()
    val serviceStatus = _serviceStatus as LiveData<Resource<ServiceStatus>>

    private fun load(): ServiceStatus {
        if (!Shizuku.pingBinder()) {
            return ServiceStatus()
        }

        val uid = Shizuku.getUid()
        val version = Shizuku.getVersion()
        val seContext = if (version >= 6) {
            try {
                Shizuku.getSELinuxContext()
            } catch (tr: Throwable) {
                LOGGER.w(tr, "getSELinuxContext")
                null
            }
        } else null
        val permissionTest = Shizuku.checkRemotePermission("android.permission.GRANT_RUNTIME_PERMISSIONS") == PackageManager.PERMISSION_GRANTED
        return ServiceStatus(uid, version, seContext, permissionTest)
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = load()
                _serviceStatus.postValue(Resource.success(status))
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _serviceStatus.postValue(Resource.error(e))
            }
        }
    }
}