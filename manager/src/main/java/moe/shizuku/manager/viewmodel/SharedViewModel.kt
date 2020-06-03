package moe.shizuku.manager.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import moe.shizuku.manager.ktx.logd
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set

private val referenceCounts = HashMap<ViewModel, AtomicInteger>()
private val cache = ArrayMap<String, ViewModel>()

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.sharedViewModels(noinline keyProducer: () -> String? = { null }, noinline viewModelProducer: () -> VM) =
        createSharedViewModelLazy({ this }, keyProducer, VM::class.qualifiedName!!, viewModelProducer)

@MainThread
inline fun <reified VM : ViewModel> Fragment.activitySharedViewModels(noinline keyProducer: () -> String? = { null }, noinline viewModelProducer: () -> VM) =
        createSharedViewModelLazy({ requireActivity() }, keyProducer, VM::class.qualifiedName!!, viewModelProducer)

@MainThread
fun <VM : ViewModel> createSharedViewModelLazy(referrerProducer: () -> ComponentActivity, keyProducer: () -> String?, className: String, viewModelProducer: () -> VM): Lazy<VM> {
    return SharedViewModelLazy(referrerProducer, keyProducer, className, viewModelProducer)
}

private class SharedViewModelLazy<VM : ViewModel>(
        private val referrerProducer: () -> ComponentActivity,
        private val keyProducer: () -> String?,
        private val className: String,
        private val viewModelProducer: () -> VM) : Lazy<VM> {

    private var cached: VM? = null

    @Suppress("UNCHECKED_CAST")
    override val value: VM
        get() {
            if (cached != null) return cached!!
            val key = className + ":" + keyProducer()
            return ((cache[key] as? VM) ?: viewModelProducer()).also { vm ->
                cached = vm
                cache[key] = vm

                if (!referenceCounts.containsKey(vm)) {
                    referenceCounts[vm] = AtomicInteger()
                }
                referenceCounts[vm]!!.incrementAndGet()

                val activity = referrerProducer()
                activity.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event != Lifecycle.Event.ON_DESTROY) return

                        val isChangingConfigurations = activity.isChangingConfigurations
                        if (referenceCounts[vm]?.decrementAndGet() == 0) {
                            referenceCounts.remove(vm)
                            if (!isChangingConfigurations) {
                                cache.values.remove(vm)
                                vm.clear()
                            }
                        }

                        logd("SharedViewModel", "$activity: cleared $vm $referenceCounts $cache")
                    }
                })

                logd("SharedViewModel", "$activity: added $vm $referenceCounts $cache")
            }
        }

    override fun isInitialized() = cached != null
}

private val clearMethod: Method? = try {
    ViewModel::class.java.getDeclaredMethod("clear").apply { isAccessible = true }
} catch (e: Throwable) {
    e.printStackTrace()
    null
}

private fun ViewModel.clear() {
    clearMethod!!.invoke(this)
}
