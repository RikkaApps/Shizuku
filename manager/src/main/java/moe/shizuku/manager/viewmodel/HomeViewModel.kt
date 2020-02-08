package moe.shizuku.manager.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.legacy.ShizukuLegacy
import moe.shizuku.manager.legacy.ShizukuLegacy.ShizukuState
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.utils.Logger
import moe.shizuku.server.IShizukuService
import java.util.*

class HomeViewModel : ViewModel() {

    val serviceStatus = MutableLiveData<Resource<ServiceStatus>>()

    private fun load(): ServiceStatus {
        var v2Status: ShizukuState
        val status = ServiceStatus()
        status.v2Status = ShizukuLegacy.ShizukuClient.getState().also { v2Status = it }
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
                Logger.LOGGER.w(tr, "getSELinuxContext")
            }
        }
        if (v2Status.code == ShizukuState.STATUS_UNAUTHORIZED) {
            val token = IShizukuService.Stub.asInterface(ShizukuService.getBinder()).token
            ShizukuLegacy.putToken(UUID.fromString(token))
            status.v2Status = ShizukuLegacy.ShizukuClient.getState()
        }
        return status
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val status = load()
                serviceStatus.postValue(Resource.success(status))
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                serviceStatus.postValue(Resource.error(e))
            }
        }
    }
}