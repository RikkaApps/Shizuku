package moe.shizuku.manager.management

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.authorization.AuthorizationManager
import moe.shizuku.manager.utils.BuildUtils
import moe.shizuku.manager.viewmodel.Resource
import moe.shizuku.manager.viewmodel.SharedViewModel
import java.util.*

class AppsViewModel : SharedViewModel() {

    val packages = MutableLiveData<Resource<List<PackageInfo>>>()
    val grantedCount = MutableLiveData<Resource<Int>>()

    fun load() {
        launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                var count = 0
                for (pi in AuthorizationManager.getPackages(PackageManager.GET_META_DATA)) {
                    if (BuildConfig.APPLICATION_ID == pi.packageName) continue
                    if (BuildUtils.atLeastQ() && pi?.applicationInfo?.metaData?.getBoolean("moe.shizuku.client.V3_SUPPORT") != true) continue
                    list.add(pi)
                    if (AuthorizationManager.granted(pi.packageName, pi.applicationInfo.uid)) count++
                }
                packages.postValue(Resource.success(list))
                grantedCount.postValue(Resource.success(count))
            } catch (e:CancellationException) {

            } catch (e:Throwable) {
                packages.postValue(Resource.error(e, null))
                grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }

    fun loadCount() {
        launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                val packages: MutableList<String> = ArrayList()
                for (pi in AuthorizationManager.getPackages(PackageManager.GET_META_DATA)) {
                    if (BuildConfig.APPLICATION_ID == pi.packageName) continue
                    list.add(pi)
                    if (AuthorizationManager.granted(pi.packageName, pi.applicationInfo.uid)) packages.add(pi.packageName)
                }
            } catch (e:CancellationException) {

            } catch (e:Throwable) {
                packages.postValue(Resource.error(e, null))
                grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }
}