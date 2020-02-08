package moe.shizuku.manager.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class SharedViewModel : ViewModel(), CoroutineScope {

    private val lifeJob: Job = SupervisorJob()

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + lifeJob

    private fun cancelJobs() {
        lifeJob.cancel()
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        SharedViewModelProviders.clear(this)
    }

    @CallSuper
    open fun onFullyCleared() {
        cancelJobs()
    }

}
