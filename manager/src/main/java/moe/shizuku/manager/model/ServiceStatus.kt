package moe.shizuku.manager.model

import rikka.shizuku.Shizuku

data class ServiceStatus(
        val uid: Int = - 1,
        val version: Int = -1,
        val seContext: String? = null
) {
    val isRunning: Boolean
        get() = Shizuku.pingBinder()
}