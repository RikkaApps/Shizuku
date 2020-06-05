package moe.shizuku.manager.starter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.topjohnwu.superuser.Shell
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.R
import moe.shizuku.manager.utils.NotificationHelper
import rikka.core.app.ForegroundIntentService

class BootCompleteService : ForegroundIntentService("BootCompleteService") {

    override fun getForegroundServiceNotificationId(): Int {
        return AppConstants.NOTIFICATION_ID_STATUS
    }

    override fun onStartForeground(): Notification {
        val builder = NotificationHelper.create(this, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_starting)
                .setOngoing(true)
        if (Build.VERSION.SDK_INT < 26) {
            builder.priority = NotificationCompat.PRIORITY_MIN
        }
        return builder.build()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreateNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_STATUS, getString(R.string.notification_channel_service_status), NotificationManager.IMPORTANCE_MIN)
        channel.setSound(null, null)
        channel.setShowBadge(false)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onHandleIntent(intent: Intent?) {
        val context = this
        if (!Shell.rootAccess()) {
            NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root)
            return
        }

        if (Starter.getCommand() == null) {
            Starter.writeFiles(context)
        }
        Shell.su(Starter.getCommand()).exec().let {
            if (it.code == 0) {
                NotificationHelper.cancel(context, AppConstants.NOTIFICATION_ID_STATUS)
            } else {
                NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_failed)
            }
        }
    }
}