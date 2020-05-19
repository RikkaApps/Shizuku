package moe.shizuku.manager.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.viewModels(
        noinline viewModelProducer: () -> VM
) = ViewModelLazy(
        { viewModelStore },
        viewModelProducer
)

inline fun <reified VM : ViewModel> Fragment.activityViewModels(
        noinline viewModelProducer: () -> VM
) = viewModels(::requireActivity, viewModelProducer)

@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModels(
        noinline ownerProducer: () -> ViewModelStoreOwner = { this },
        noinline viewModelProducer: () -> VM
) = ViewModelLazy(
        { ownerProducer().viewModelStore },
        viewModelProducer
)

class ViewModelLazy<VM : ViewModel>(
        private val storeProducer: () -> ViewModelStore,
        private val viewModelProducer: () -> VM
) : Lazy<VM> {
    private var cached: VM? = null

    @Suppress("UNCHECKED_CAST")
    override val value: VM
        get() {
            val viewModel = cached
            return (if (viewModel == null) {
                val store = storeProducer()
                ViewModelProvider(store, object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return viewModelProducer() as T
                    }

                }).get(ViewModel::class.java).also {
                    cached = it as VM
                }
            } else {
                viewModel
            }) as VM
        }

    override fun isInitialized() = cached != null
}
