package moe.shizuku.manager.legacy

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.utils.BuildUtils
import moe.shizuku.manager.utils.ktx.toHtml
import moe.shizuku.server.IShizukuService
import rikka.html.text.HtmlCompat
import java.util.*

abstract class AuthorizationActivity : AppActivity() {

    companion object {
        private const val ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT"
    }

    val isV3: Boolean by lazy {
        intent.getBooleanExtra(ShizukuApiConstants.EXTRA_PRE_23_IS_V3, false)
    }

    fun getLegacyServerState(): ShizukuLegacy.ShizukuState {
        if (BuildUtils.atLeast29()) {
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

    fun checkNotLegacyOnApi30(): Boolean {
        if (!isV3 && BuildUtils.atLeast29()) {
            val componentName = callingActivity ?: return false
            val ai = try {
                packageManager.getApplicationInfo(componentName.packageName, PackageManager.GET_META_DATA)
            } catch (e: Throwable) {
                return false
            }
            val label = try {
                ai.loadLabel(packageManager)
            } catch (e: Exception) {
                ai.packageName
            }
            val v3Support = ai.metaData?.getBoolean("moe.shizuku.client.V3_SUPPORT") == true
            if (v3Support) {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.auth_requesting_legacy_title, label))
                        .setMessage(getString(R.string.auth_requesting_legacy_message, label).toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE))
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton(R.string.open_manager) { _, _ ->
                            startActivity(Intent(this, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                        .setOnDismissListener {
                            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
            } else {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.auth_legacy_not_support_title, label))
                        .setMessage(getString(R.string.auth_legacy_not_support_message, label).toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE))
                        .setPositiveButton(android.R.string.ok, null)
                        .setOnDismissListener {
                            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
            }

            return false
        }
        return true
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