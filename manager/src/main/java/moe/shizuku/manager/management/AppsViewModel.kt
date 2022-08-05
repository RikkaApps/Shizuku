package moe.shizuku.manager.management

import android.content.Context
import android.content.pm.PackageInfo
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.authorization.AuthorizationManager
import rikka.lifecycle.Resource
import rikka.lifecycle.activitySharedViewModels
import rikka.lifecycle.sharedViewModels
import java.util.*

@MainThread
fun ComponentActivity.appsViewModel() = sharedViewModels { AppsViewModel(this) }

@MainThread
fun Fragment.appsViewModel() = activitySharedViewModels { AppsViewModel(requireContext()) }

class AppsViewModel(context: Context) : ViewModel() {

    private val _packages = MutableLiveData<Resource<List<PackageInfo>>>()
    val packages = _packages as LiveData<Resource<List<PackageInfo>>>

    private val _grantedCount = MutableLiveData<Resource<Int>>()
    val grantedCount = _grantedCount as LiveData<Resource<Int>>

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                var count = 0
                for (pi in AuthorizationManager.getPackages()) {
                    list.add(pi)
                    if (AuthorizationManager.granted(pi.packageName, pi.applicationInfo.uid)) count++
                }
                _packages.postValue(Resource.success(list))
                _grantedCount.postValue(Resource.success(count))
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _packages.postValue(Resource.error(e, null))
                _grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }

    fun loadCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                val packages: MutableList<String> = ArrayList()
                for (pi in AuthorizationManager.getPackages()) {
                    list.add(pi)
                    if (AuthorizationManager.granted(
                            pi.packageName,
                            pi.applicationInfo.uid
                        )
                    ) packages.add(pi.packageName)
                }
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _packages.postValue(Resource.error(e, null))
                _grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }
}
