package moe.shizuku.manager.adb

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import moe.shizuku.manager.AppConstants

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object AdbAutostartScheduler {

    const val JOB_ID_PERIODIC = 0x5A1A0001
    const val JOB_ID_ONESHOT = 0x5A1A0002

    private const val PERIOD_MS = 15L * 60L * 1000L

    fun schedule(context: Context) {
        val js = context.getSystemService(JobScheduler::class.java) ?: return
        val component = ComponentName(context, AdbAutostartJobService::class.java)
        val wifi = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val periodic = JobInfo.Builder(JOB_ID_PERIODIC, component)
            .setRequiredNetwork(wifi)
            .setPersisted(true)
            .setPeriodic(PERIOD_MS)
            .build()

        val oneshot = JobInfo.Builder(JOB_ID_ONESHOT, component)
            .setRequiredNetwork(wifi)
            .setPersisted(true)
            .setMinimumLatency(0)
            .setBackoffCriteria(30_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
            .build()

        // Rescheduling an existing id stops it if it's running, and this runs in the job's own process.
        for (job in listOf(periodic, oneshot)) {
            if (js.getPendingJob(job.id)?.requiredNetwork == job.requiredNetwork) continue
            val result = js.schedule(job)
            Log.i(AppConstants.TAG, "AdbAutostartScheduler: scheduled job ${job.id} result=$result")
        }
    }

    fun cancel(context: Context) {
        val js = context.getSystemService(JobScheduler::class.java) ?: return
        js.cancel(JOB_ID_PERIODIC)
        js.cancel(JOB_ID_ONESHOT)
    }
}
