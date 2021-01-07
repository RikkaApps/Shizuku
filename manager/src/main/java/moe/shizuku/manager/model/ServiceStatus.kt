package moe.shizuku.manager.model

import moe.shizuku.api.ShizukuService

data class ServiceStatus(
        val uid: Int = - 1,
        val version: Int = -1,
        val seContext: String? = null
) {
    val isRunning: Boolean
        get() = ShizukuService.pingBinder()
}