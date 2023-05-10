package moe.shizuku.manager.adb

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import rikka.core.ktx.unsafeLazy
import java.net.ConnectException

@TargetApi(Build.VERSION_CODES.R)
class AdbPairingService : Service() {

    companion object {

        const val notificationChannel = "adb_pairing"

        private const val tag = "AdbPairingService"

        private const val notificationId = 1
        private const val replyRequestId = 1
        private const val stopRequestId = 2
        private const val retryRequestId = 3
        private const val startAction = "start"
        private const val stopAction = "stop"
        private const val replyAction = "reply"
        private const val remoteInputResultKey = "paring_code"
        private const val portKey = "paring_code"

        fun startIntent(context: Context): Intent {
            return Intent(context, AdbPairingService::class.java).setAction(startAction)
        }

        private fun stopIntent(context: Context): Intent {
            return Intent(context, AdbPairingService::class.java).setAction(stopAction)
        }

        private fun replyIntent(context: Context, port: Int): Intent {
            return Intent(context, AdbPairingService::class.java).setAction(replyAction).putExtra(portKey, port)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val port = MutableLiveData<Int>()
    private var adbMdns: AdbMdns? = null

    private val observer = Observer<Int> { port ->
        Log.i(tag, "Pairing service port: $port")

        // Since the service could be killed before user finishing input,
        // we need to put the port into Intent
        val notification = createInputNotification(port)

        getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    private var started = false

    override fun onCreate() {
        super.onCreate()

        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                notificationChannel,
                getString(R.string.notification_channel_adb_pairing),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                setShowBadge(false)
                setAllowBubbles(false)
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = when (intent?.action) {
            startAction -> {
                onStart()
            }
            replyAction -> {
                val code = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(remoteInputResultKey) ?: ""
                val port = intent.getIntExtra(portKey, -1)
                if (port != -1) {
                    onInput(code.toString(), port)
                } else {
                    onStart()
                }
            }
            stopAction -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                null
            }
            else -> {
                return START_NOT_STICKY
            }
        }
        if (notification != null) {
            try {
                startForeground(notificationId, notification)
            } catch (e: Throwable) {
                Log.e(tag, "startForeground failed", e)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException) {
                    getSystemService(NotificationManager::class.java).notify(notificationId, notification)
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun startSearch() {
        if (started) return
        started = true
        adbMdns = AdbMdns(this, AdbMdns.TLS_PAIRING, port).apply { start() }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            port.observeForever(observer)
        } else {
            handler.post { port.observeForever(observer) }
        }
    }

    private fun stopSearch() {
        if (!started) return
        started = false
        adbMdns?.stop()

        if (Looper.myLooper() == Looper.getMainLooper()) {
            port.removeObserver(observer)
        } else {
            handler.post { port.removeObserver(observer) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSearch()
    }

    private fun onStart(): Notification {
        startSearch()
        return searchingNotification
    }

    private fun onInput(code: String, port: Int): Notification {
        GlobalScope.launch(Dispatchers.IO) {
            val host = "127.0.0.1"

            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                return@launch
            }

            AdbPairingClient(host, port, code, key).runCatching {
                start()
            }.onFailure {
                handleResult(false, it)
            }.onSuccess {
                handleResult(it, null)
            }
        }

        return workingNotification
    }

    private fun handleResult(success: Boolean, exception: Throwable?) {
        stopForeground(STOP_FOREGROUND_REMOVE)

        val title: String
        val text: String?

        if (success) {
            Log.i(tag, "Pair succeed")

            title = getString(R.string.notification_adb_pairing_succeed_title)
            text = getString(R.string.notification_adb_pairing_succeed_text)

            stopSearch()
        } else {
            title = getString(R.string.notification_adb_pairing_failed_title)

            text = when (exception) {
                is ConnectException -> {
                    getString(R.string.cannot_connect_port)
                }
                is AdbInvalidPairingCodeException -> {
                    getString(R.string.paring_code_is_wrong)
                }
                is AdbKeyException -> {
                    getString(R.string.adb_error_key_store)
                }
                else -> {
                    exception?.let { Log.getStackTraceString(it) }
                }
            }

            if (exception != null) {
                Log.w(tag, "Pair failed", exception)
            } else {
                Log.w(tag, "Pair failed")
            }
        }

        getSystemService(NotificationManager::class.java).notify(
            notificationId,
            Notification.Builder(this, notificationChannel)
                .setColor(getColor(R.color.notification))
                .setSmallIcon(R.drawable.ic_system_icon)
                .setContentTitle(title)
                .setContentText(text)
                /*.apply {
                    if (!success) {
                        addAction(retryNotificationAction)
                    }
                }*/
                .build()
        )
    }

    private val stopNotificationAction by unsafeLazy {
        val pendingIntent = PendingIntent.getService(
            this,
            stopRequestId,
            stopIntent(this),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else
                0
        )

        Notification.Action.Builder(
            null,
            getString(R.string.notification_adb_pairing_stop_searching),
            pendingIntent
        )
            .build()
    }

    private val retryNotificationAction by unsafeLazy {
        val pendingIntent = PendingIntent.getService(
            this,
            retryRequestId,
            startIntent(this),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else
                0
        )

        Notification.Action.Builder(
            null,
            getString(R.string.notification_adb_pairing_retry),
            pendingIntent
        )
            .build()
    }

    private val replyNotificationAction by unsafeLazy {
        val remoteInput = RemoteInput.Builder(remoteInputResultKey).run {
            setLabel(getString(R.string.dialog_adb_pairing_paring_code))
            build()
        }

        val pendingIntent = PendingIntent.getForegroundService(
            this,
            replyRequestId,
            replyIntent(this, -1),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        Notification.Action.Builder(
            null,
            getString(R.string.notification_adb_pairing_input_paring_code),
            pendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun replyNotificationAction(port: Int): Notification.Action {
        // Ensure pending intent is created
        val action = replyNotificationAction

        PendingIntent.getForegroundService(
            this,
            replyRequestId,
            replyIntent(this, port),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        return action
    }

    private val searchingNotification by unsafeLazy {
        Notification.Builder(this, notificationChannel)
            .setColor(getColor(R.color.notification))
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentTitle(getString(R.string.notification_adb_pairing_searching_for_service_title))
            .addAction(stopNotificationAction)
            .build()
    }

    private fun createInputNotification(port: Int): Notification {
        return Notification.Builder(this, notificationChannel)
            .setColor(getColor(R.color.notification))
            .setContentTitle(getString(R.string.notification_adb_pairing_service_found_title))
            .setSmallIcon(R.drawable.ic_system_icon)
            .addAction(replyNotificationAction(port))
            .build()
    }

    private val workingNotification by unsafeLazy {
        Notification.Builder(this, notificationChannel)
            .setColor(getColor(R.color.notification))
            .setContentTitle(getString(R.string.notification_adb_pairing_working_title))
            .setSmallIcon(R.drawable.ic_system_icon)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
