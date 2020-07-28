package moe.shizuku.manager

import android.os.Bundle
import moe.shizuku.api.BinderContainer
import moe.shizuku.api.ShizukuApiConstants.EXTRA_BINDER
import moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_TOKEN
import moe.shizuku.api.ShizukuProvider
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.utils.Logger.LOGGER

class ShizukuManagerProvider : ShizukuProvider() {

    companion object {

        private const val METHOD_SEND_USER_SERVICE = "sendUserService"
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (extras == null) return null

        return if (method == METHOD_SEND_USER_SERVICE) {
            try {
                extras.classLoader = BinderContainer::class.java.classLoader

                val token = extras.getString(USER_SERVICE_ARG_TOKEN) ?: return null
                val binder = extras.getParcelable<BinderContainer>(EXTRA_BINDER)?.binder ?: return null

                ShizukuService.sendUserService(binder, Bundle().apply {
                    putString(USER_SERVICE_ARG_TOKEN, token)
                })

                val reply = Bundle()
                reply.putParcelable(EXTRA_BINDER, BinderContainer(ShizukuService.getBinder()))
                reply
            } catch (e: Throwable) {
                LOGGER.e(e, "sendUserService")
                null
            }
        } else {
            super.call(method, arg, extras)
        }
    }
}