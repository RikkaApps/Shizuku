package moe.shizuku.manager

import android.os.Bundle
import moe.shizuku.api.BinderContainer
import moe.shizuku.manager.utils.Logger.LOGGER
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_TOKEN
import rikka.shizuku.ShizukuProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ShizukuManagerProvider : ShizukuProvider() {

    companion object {
        private const val EXTRA_BINDER = "moe.shizuku.privileged.api.intent.extra.BINDER"
        private const val METHOD_SEND_USER_SERVICE = "sendUserService"
    }

    override fun onCreate(): Boolean {
        disableAutomaticSuiInitialization()
        return super.onCreate()
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (extras == null) return null

        return if (method == METHOD_SEND_USER_SERVICE) {
            try {
                extras.classLoader = BinderContainer::class.java.classLoader

                val token = extras.getString(USER_SERVICE_ARG_TOKEN) ?: return null
                val binder = extras.getParcelable<BinderContainer>(EXTRA_BINDER)?.binder ?: return null

                val countDownLatch = CountDownLatch(1)
                val reply = Bundle()

                val listener = object : Shizuku.OnBinderReceivedListener {

                    override fun onBinderReceived() {
                        Shizuku.attachUserService(binder, Bundle().apply {
                            putString(USER_SERVICE_ARG_TOKEN, token)
                        })
                        reply.putParcelable(EXTRA_BINDER, BinderContainer(Shizuku.getBinder()))

                        Shizuku.removeBinderReceivedListener(this)

                        countDownLatch.countDown()
                    }
                }

                Shizuku.addBinderReceivedListenerSticky(listener)

                return try {
                    countDownLatch.await(5, TimeUnit.SECONDS)
                    reply
                } catch (e: TimeoutException) {
                    LOGGER.e(e, "Binder not received in 5s")
                    null
                }
            } catch (e: Throwable) {
                LOGGER.e(e, "sendUserService")
                null
            }
        } else {
            super.call(method, arg, extras)
        }
    }
}
