package moe.shizuku.manager.adb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.MutableLiveData
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket

class AdbMdns(context: Context, private val serviceType: String,
              private val port: MutableLiveData<Int>) {
    private var registered = false
    private var running = false
    private var serviceName: String? = null
    private val listener: DiscoveryListener
    private val nsdManager: NsdManager = context.getSystemService(NsdManager::class.java)

    fun start() {
        running = true
        if (!registered) {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
        }
    }

    fun stop() {
        running = false
        if (registered) {
            nsdManager.stopServiceDiscovery(listener)
        }
    }

    private fun onDiscoveryStart() {
        registered = true
    }

    private fun onDiscoveryStop() {
        registered = false
    }

    private fun onServiceFound(info: NsdServiceInfo) {
        nsdManager.resolveService(info, ResolveListener(this))
    }

    private fun onServiceLost(info: NsdServiceInfo) {
        if (info.serviceName == serviceName) port.postValue(-1)
    }

    private fun onServiceResolved(resolvedService: NsdServiceInfo) {
        if (running && resolvedService.host.let { host ->
                    NetworkInterface.getNetworkInterfaces()
                            .asSequence()
                            .any { networkInterface ->
                                networkInterface.inetAddresses
                                        .asSequence()
                                        .any { host.hostAddress == it.hostAddress }
                            }
                } && !isPortAvailable(resolvedService.port)) {
            serviceName = resolvedService.serviceName
            port.postValue(resolvedService.port)
        }
    }

    private fun isPortAvailable(port: Int) = try {
        ServerSocket().use { serverSocket ->
            serverSocket.reuseAddress = false
            serverSocket.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1)
            true
        }
    } catch (e: Exception) {
        false
    }

    internal class DiscoveryListener(private val adbMdns: AdbMdns) : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(str: String) {
            adbMdns.onDiscoveryStart()
        }

        override fun onStartDiscoveryFailed(str: String, i: Int) {}

        override fun onDiscoveryStopped(str: String) {
            adbMdns.onDiscoveryStop()
        }

        override fun onStopDiscoveryFailed(str: String, i: Int) {}

        override fun onServiceFound(nsdServiceInfo: NsdServiceInfo) {
            adbMdns.onServiceFound(nsdServiceInfo)
        }

        override fun onServiceLost(nsdServiceInfo: NsdServiceInfo) {
            adbMdns.onServiceLost(nsdServiceInfo)
        }
    }

    internal class ResolveListener(private val adbMdns: AdbMdns)
        : NsdManager.ResolveListener {
        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, i: Int) {}

        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo) {
            adbMdns.onServiceResolved(nsdServiceInfo)
        }

    }

    companion object {
        const val TLS_CONNECT = "_adb-tls-connect._tcp"
        const val TLS_PAIRING = "_adb-tls-pairing._tcp"
    }

    init {
        listener = DiscoveryListener(this)
    }
}