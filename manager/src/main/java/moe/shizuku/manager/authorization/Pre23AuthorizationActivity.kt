package moe.shizuku.manager.authorization

import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.server.IShizukuService
import rikka.core.ktx.unsafeLazy
import rikka.html.text.HtmlCompat
import java.util.*

class Pre23AuthorizationActivity : AppActivity() {

    companion object {

        private const val ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT"

        // ShizukuClientHelperPre23.java
        private const val KEY_TOKEN_MOST_SIG = "moe.shizuku.privilege.api.token_most"
        private const val KEY_TOKEN_LEAST_SIG = "moe.shizuku.privilege.api.token_least"

        /**
         * Activity result: ok, token is returned.
         */
        private inline val RESULT_OK get() = Activity.RESULT_OK

        /**
         * Activity result: user denied request (only API pre-23).
         */
        private inline val RESULT_CANCELED get() = Activity.RESULT_CANCELED

        /**
         * Activity result: error, such as manager app itself not authorized.
         */
        private const val RESULT_ERROR = 1
    }

    private val isV3 by unsafeLazy {
        intent.getBooleanExtra(ShizukuApiConstants.EXTRA_PRE_23_IS_V3, false)
    }

    private lateinit var callingComponent: ComponentName

    private fun enforceCaller(): Boolean {
        val componentName = callingActivity ?: return false
        callingComponent = componentName
        return true
    }

    private fun enforceNonLegacy(): Boolean {
        if (isV3) return true

        val ai = try {
            packageManager.getApplicationInfo(callingComponent.packageName, PackageManager.GET_META_DATA)
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
                        setResult(RESULT_ERROR)
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
                        setResult(RESULT_ERROR)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
        }

        return false
    }

    private fun setResult(granted: Boolean, packageName: String?) {
        if (granted) {
            var token: UUID? = null
            val service = IShizukuService.Stub.asInterface(ShizukuService.getBinder())
            if (service != null) {
                try {
                    token = UUID.fromString(service.token)
                } catch (ignored: Throwable) {
                }
            }

            val intent = Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
            if (token == null) {
                setResult(RESULT_ERROR, intent)
                return
            }
            intent.putExtra(KEY_TOKEN_MOST_SIG, token.mostSignificantBits)
                    .putExtra(KEY_TOKEN_LEAST_SIG, token.leastSignificantBits)
            setResult(RESULT_OK, intent)
        } else {
            setResult(RESULT_CANCELED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!enforceCaller()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (!enforceNonLegacy()) {
            return
        }

        if (!ShizukuService.pingBinder()) {
            AlertDialog.Builder(this)
                    .setMessage(R.string.auth_cannot_connect)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.open_manager) { dialog, which ->
                        startActivity(Intent(this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    .setOnDismissListener { dialog: DialogInterface? ->
                        setResult(RESULT_ERROR)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            return
        }

        val packageName = callingComponent.packageName
        val mode = try {
            if (ShizukuService.getUid() == 0) "root" else "adb"
        } catch (ignored: Throwable) {
            "unknown"
        }

        val pi: PackageInfo
        pi = try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (ignored: PackageManager.NameNotFoundException) {
            Log.wtf(AppConstants.TAG, "auth | package not found: $packageName")
            setResult(RESULT_ERROR)
            finish()
            return
        }

        if (AuthorizationManager.granted(packageName, pi.applicationInfo.uid)) {
            setResult(true, packageName)
            finish()
            return
        }

        val name = pi.applicationInfo.loadLabel(packageManager)
        val message: CharSequence = getString(R.string.auth_message, name, mode).toHtml()
        val dialog: Dialog = AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.auth_allow) { _, _ ->
                    AuthorizationManager.grant(packageName, pi.applicationInfo.uid)
                    setResult(true, packageName)
                }
                .setNegativeButton(R.string.auth_deny) { _, _ ->
                    AuthorizationManager.revoke(packageName, pi.applicationInfo.uid)
                    setResult(false, packageName)
                }
                .setOnDismissListener { finish() }
                .setCancelable(false)
                .create()
        dialog.setOnShowListener { d: DialogInterface ->
            val alertDialog = d as AlertDialog
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).filterTouchesWhenObscured = true
        }
        dialog.show()
        val textView = dialog.findViewById<TextView>(android.R.id.message)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.request_dialog_text_size))
    }
}