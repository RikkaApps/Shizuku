package moe.shizuku.server.api

import android.os.*
import moe.shizuku.server.utils.Logger.LOGGER
import java.io.FileDescriptor

open class SystemServiceBinder<T : IInterface>(val name: String, private val converter: (binder: IBinder) -> T) : IBinder {

    private var binderCache: IBinder? = null
    private var serviceCache: T? = null

    private val binder: IBinder?
        get() = binderCache ?: ServiceManager.getService(name)?.let {
            LOGGER.v("get service $name")

            try {
                it.linkToDeath({
                    binderCache = null
                    serviceCache = null
                }, 0)

                // save binder only if linkToDeath succeed
                binderCache = it
            } catch (e: Throwable) {
                LOGGER.w(e, "linkToDeath $name failed")
            }
            it
        }

    val service: T?
        get() = serviceCache ?: binder?.let {
            serviceCache = converter(this)
            serviceCache
        }

    @Throws(RemoteException::class)
    override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return binder?.let {
            try {
                it.transact(code, data, reply, flags)
            } catch (e: DeadObjectException) {
                LOGGER.w(e, "transact $name $code failed")

                // try again when DeadObjectException
                binderCache = null

                binder?.transact(code, data, reply, flags)
            }
        } ?: false
    }

    @Throws(RemoteException::class)
    override fun getInterfaceDescriptor(): String? {
        return binder?.interfaceDescriptor
    }

    override fun pingBinder(): Boolean {
        return binder?.pingBinder() ?: false
    }

    override fun isBinderAlive(): Boolean {
        return binder?.isBinderAlive ?: false
    }

    override fun queryLocalInterface(descriptor: String): IInterface? {
        return binder?.queryLocalInterface(descriptor)
    }

    @Throws(RemoteException::class)
    override fun dump(fd: FileDescriptor, args: Array<String>?) {
        binder?.dump(fd, args)
    }

    @Throws(RemoteException::class)
    override fun dumpAsync(fd: FileDescriptor, args: Array<String>?) {
        binder?.dumpAsync(fd, args)
    }

    @Throws(RemoteException::class)
    override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
        binder?.linkToDeath(recipient, flags)
    }

    override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
        return binder?.unlinkToDeath(recipient, flags) ?: false
    }
}