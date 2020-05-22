package moe.shizuku.manager.home

import android.service.quicksettings.TileService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.utils.Logger.LOGGER
import moe.shizuku.manager.viewmodel.Resource

class HomeViewModel : ViewModel() {

    private val _serviceStatus = MutableLiveData<Resource<ServiceStatus>>()
    val serviceStatus = _serviceStatus as LiveData<Resource<ServiceStatus>>

    private fun load(): ServiceStatus {
        val status = ServiceStatus()
        if (ShizukuService.getBinder() == null)
            return status

        if (!ShizukuService.pingBinder()) {
            ShizukuService.setBinder(null)
            return status
        }

        status.uid = ShizukuService.getUid()
        status.version = ShizukuService.getVersion()
        if (status.version >= 6) {
            try {
                status.seContext = ShizukuService.getSELinuxContext()
            } catch (tr: Throwable) {
                LOGGER.w(tr, "getSELinuxContext")
            }
        }
        return status
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