package moe.shizuku.manager.adb

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import moe.shizuku.manager.AppConstants

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AdbAutostartJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(AppConstants.TAG, "AdbAutostartJobService: onStartJob id=${params.jobId}")

        if (!AdbAutostart.canAutostart(this)) {
            Log.i(AppConstants.TAG, "AdbAutostartJobService: canAutostart=false, skipping")
            return false
        }

        AdbAutostart.start(this) { success ->
            jobFinished(params, !success)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(AppConstants.TAG, "AdbAutostartJobService: onStopJob id=${params.jobId}")
        return true
    }
}
