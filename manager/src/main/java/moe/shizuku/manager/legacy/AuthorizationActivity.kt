package moe.shizuku.manager.legacy

import android.content.Intent
import androidx.annotation.Keep
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.app.BaseActivity
import moe.shizuku.manager.utils.BuildUtils
import moe.shizuku.server.IShizukuService
import java.util.*

abstract class AuthorizationActivity : BaseActivity() {

    companion object {
        private const val ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT"
    }

    val isV3: Boolean
        get() = intent.getBooleanExtra(ShizukuApiConstants.EXTRA_PRE_23_IS_V3, false)

    fun getLegacyServerState(): ShizukuLegacy.ShizukuState {
        if (BuildUtils.atLeastR()) {
            return ShizukuLegacy.ShizukuState.createUnknown()
        }

        var state: ShizukuLegacy.ShizukuState? = null
        runBlocking {
            lifecycleScope.launch(Dispatchers.IO) {
                state = ShizukuLegacy.ShizukuClient.getState() ?: ShizukuLegacy.ShizukuState.createUnknown()
            }.join()
        }
        return state!!
    }

    fun setResult(granted: Boolean, packageName: String?) {
        if (granted) {
            var token: UUID? = null
            if (isV3) {
                val service = IShizukuService.Stub.asInterface(ShizukuService.getBinder())
                if (service != null) {
                    try {
                        token = UUID.fromString(service.token)
                    } catch (ignored: Throwable) {
                    }
                }
            } else {
                token = ShizukuLegacy.getToken()
            }
            val intent = Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
            if (token == null) {
                setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR, intent)
                return
            }
            intent.putExtra(ShizukuLegacy.EXTRA_TOKEN_MOST_SIG, token.mostSignificantBits)
                    .putExtra(ShizukuLegacy.EXTRA_TOKEN_LEAST_SIG, token.leastSignificantBits)
            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_OK, intent)
        } else {
            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_USER_DENIED)
        }
    }
}