package moe.shizuku.manager.adb

import android.util.Log
import moe.shizuku.manager.adb.AdbProtocol.ADB_AUTH_RSAPUBLICKEY
import moe.shizuku.manager.adb.AdbProtocol.ADB_AUTH_SIGNATURE
import moe.shizuku.manager.adb.AdbProtocol.ADB_AUTH_TOKEN
import moe.shizuku.manager.adb.AdbProtocol.A_AUTH
import moe.shizuku.manager.adb.AdbProtocol.A_CLSE
import moe.shizuku.manager.adb.AdbProtocol.A_CNXN
import moe.shizuku.manager.adb.AdbProtocol.A_MAXDATA
import moe.shizuku.manager.adb.AdbProtocol.A_OKAY
import moe.shizuku.manager.adb.AdbProtocol.A_OPEN
import moe.shizuku.manager.adb.AdbProtocol.A_STLS
import moe.shizuku.manager.adb.AdbProtocol.A_STLS_VERSION
import moe.shizuku.manager.adb.AdbProtocol.A_VERSION
import moe.shizuku.manager.adb.AdbProtocol.A_WRTE
import moe.shizuku.manager.ktx.logd
import rikka.core.util.BuildUtils
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocket

private const val TAG = "AdbClient"

class AdbClient(
    private val host: String,
    private val port: Int,
    private val key: AdbKey,
    private val timeoutMillis: Int? = null,
) : Closeable {

    private lateinit var socket: Socket
    private lateinit var plainInputStream: DataInputStream
    private lateinit var plainOutputStream: DataOutputStream

    private var useTls = false

    private lateinit var tlsSocket: SSLSocket
    private lateinit var tlsInputStream: DataInputStream
    private lateinit var tlsOutputStream: DataOutputStream

    private val inputStream get() = if (useTls) tlsInputStream else plainInputStream
    private val outputStream get() = if (useTls) tlsOutputStream else plainOutputStream
    private var operationDeadlineNanos = 0L
    private var timeoutTask: ScheduledFuture<*>? = null
    @Volatile private var operationTimedOut = false

    fun connect() {
        beginOperation()
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(host, port), remainingTimeoutMillis())
            socket.tcpNoDelay = true
            socket.soTimeout = remainingTimeoutMillis()
            plainInputStream = DataInputStream(socket.getInputStream())
            plainOutputStream = DataOutputStream(socket.getOutputStream())

            write(A_CNXN, A_VERSION, A_MAXDATA, "host::")

            var message = read()
            if (message.command == A_STLS) {
                if (!BuildUtils.atLeast29) {
                    error("Connect to adb with TLS is not supported before Android 9")
                }
                write(A_STLS, A_STLS_VERSION, 0)

                val sslContext = key.sslContext
                tlsSocket = sslContext.socketFactory.createSocket(socket, host, port, true) as SSLSocket
                tlsSocket.soTimeout = remainingTimeoutMillis()
                tlsSocket.startHandshake()
                Log.d(TAG, "Handshake succeeded.")

                tlsInputStream = DataInputStream(tlsSocket.inputStream)
                tlsOutputStream = DataOutputStream(tlsSocket.outputStream)
                useTls = true

                message = read()
            } else if (message.command == A_AUTH) {
                if (message.command != A_AUTH && message.arg0 != ADB_AUTH_TOKEN) error("not A_AUTH ADB_AUTH_TOKEN")
                write(A_AUTH, ADB_AUTH_SIGNATURE, 0, key.sign(message.data))

                message = read()
                if (message.command != A_CNXN) {
                    write(A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, key.adbPublicKey)
                    message = read()
                }
            }

            if (message.command != A_CNXN) error("not A_CNXN")
        } catch (e: java.io.IOException) {
            throw timeoutExceptionIfNeeded(e)
        }
    }

    fun shellCommand(command: String, listener: ((ByteArray) -> Unit)?) {
        beginOperation()
        try {
            val localId = 1
            write(A_OPEN, localId, 0, "shell:$command")

            var message = read()
            when (message.command) {
                A_OKAY -> {
                    while (true) {
                        message = read()
                        val remoteId = message.arg0
                        if (message.command == A_WRTE) {
                            if (message.data_length > 0) {
                                listener?.invoke(message.data!!)
                            }
                            write(A_OKAY, localId, remoteId)
                        } else if (message.command == A_CLSE) {
                            write(A_CLSE, localId, remoteId)
                            break
                        } else {
                            error("not A_WRTE or A_CLSE")
                        }
                    }
                }
                A_CLSE -> {
                    val remoteId = message.arg0
                    write(A_CLSE, localId, remoteId)
                }
                else -> {
                    error("not A_OKAY or A_CLSE")
                }
            }
        } catch (e: java.io.IOException) {
            throw timeoutExceptionIfNeeded(e)
        }
    }

    private fun write(command: Int, arg0: Int, arg1: Int, data: ByteArray? = null) = write(AdbMessage(command, arg0, arg1, data))

    private fun write(command: Int, arg0: Int, arg1: Int, data: String) = write(AdbMessage(command, arg0, arg1, data))

    private fun write(message: AdbMessage) {
        outputStream.write(message.toByteArray())
        outputStream.flush()
        Log.d(TAG, "write ${message.toStringShort()}")
    }

    private fun read(): AdbMessage {
        val readTimeout = remainingTimeoutMillis()
        socket.soTimeout = readTimeout
        if (useTls) tlsSocket.soTimeout = readTimeout

        val buffer = ByteBuffer.allocate(AdbMessage.HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN)

        inputStream.readFully(buffer.array(), 0, 24)

        val command = buffer.int
        val arg0 = buffer.int
        val arg1 = buffer.int
        val dataLength = buffer.int
        val checksum = buffer.int
        val magic = buffer.int
        val data: ByteArray?
        if (dataLength >= 0) {
            data = ByteArray(dataLength)
            inputStream.readFully(data, 0, dataLength)
        } else {
            data = null
        }
        val message = AdbMessage(command, arg0, arg1, dataLength, checksum, magic, data)
        message.validateOrThrow()
        Log.d(TAG, "read ${message.toStringShort()}")
        return message
    }

    private fun beginOperation() {
        val timeout = timeoutMillis ?: return
        require(timeout > 0) { "ADB timeout must be positive" }
        if (operationDeadlineNanos != 0L) {
            remainingTimeoutMillis()
            return
        }

        operationDeadlineNanos = System.nanoTime() + timeout * 1_000_000L
        timeoutTask = timeoutExecutor.schedule({
            operationTimedOut = true
            if (this::socket.isInitialized) {
                try {
                    socket.close()
                } catch (_: Exception) {
                }
            }
        }, timeout.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun remainingTimeoutMillis(): Int {
        if (timeoutMillis == null) return 0
        val remainingNanos = operationDeadlineNanos - System.nanoTime()
        if (remainingNanos <= 0) {
            operationTimedOut = true
            throw SocketTimeoutException("ADB operation timed out")
        }
        return ((remainingNanos + 999_999L) / 1_000_000L).coerceAtLeast(1L).toInt()
    }

    private fun timeoutExceptionIfNeeded(cause: java.io.IOException): java.io.IOException {
        if (!operationTimedOut) return cause
        return SocketTimeoutException("ADB operation timed out").also { it.initCause(cause) }
    }

    override fun close() {
        timeoutTask?.cancel(false)
        try {
            plainInputStream.close()
        } catch (e: Throwable) {
        }
        try {
            plainOutputStream.close()
        } catch (e: Throwable) {
        }
        try {
            socket.close()
        } catch (e: Exception) {
        }

        if (useTls) {
            try {
                tlsInputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsOutputStream.close()
            } catch (e: Throwable) {
            }
            try {
                tlsSocket.close()
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val timeoutExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "AdbClient-timeouts").apply { isDaemon = true }
        }
    }
}
