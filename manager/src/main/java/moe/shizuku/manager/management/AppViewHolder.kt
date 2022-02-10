package moe.shizuku.manager.management

import android.content.pm.PackageInfo
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.authorization.AuthorizationManager
import moe.shizuku.manager.databinding.AppListItemBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.utils.AppIconCache
import moe.shizuku.manager.utils.ShizukuSystemApis
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class AppViewHolder(private val binding: AppListItemBinding) : BaseViewHolder<PackageInfo>(binding.root), View.OnClickListener {

    companion object {
        @JvmField
        val CREATOR = Creator<PackageInfo> { inflater: LayoutInflater, parent: ViewGroup? -> AppViewHolder(AppListItemBinding.inflate(inflater, parent, false)) }
    }


    private val icon get() = binding.icon
    private val name get() = binding.title
    private val pkg get() = binding.summary
    private val switchWidget get() = binding.switchWidget
    private val root get() = binding.requiresRoot

    init {
        itemView.filterTouchesWhenObscured = true
        itemView.setOnClickListener(this)
    }

    private inline val packageName get() = data.packageName
    private inline val ai get() = data.applicationInfo
    private inline val uid get() = ai.uid

    private var loadIconJob: Job? = null

    override fun onClick(v: View) {
        val context = v.context
        try {
            if (AuthorizationManager.granted(packageName, uid)) {
                AuthorizationManager.revoke(packageName, uid)
            } else {
                AuthorizationManager.grant(packageName, uid)
            }
        } catch (e: SecurityException) {
            val uid = try {
                Shizuku.getUid()
            } catch (ex: Throwable) {
                return
            }
            if (uid != 0) {
                val dialog = MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.app_management_dialog_adb_is_limited_title)
                        .setMessage(context.getString(R.string.app_management_dialog_adb_is_limited_message, Helps.ADB.get()).toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE))
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                dialog.setOnShowListener {
                    (it as AlertDialog).findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
                }
                try {
                    dialog.show()
                } catch (ignored: Throwable) {
                }
            }
        }
        adapter.notifyItemChanged(adapterPosition, Any())
    }

    override fun onBind() {
        val pm = itemView.context.packageManager
        val userId = UserHandleCompat.getUserId(uid)
        icon.setImageDrawable(ai.loadIcon(pm))
        name.text = if (userId != UserHandleCompat.myUserId()) {
            val userInfo = ShizukuSystemApis.getUserInfo(userId)
            "${ai.loadLabel(pm)} - ${userInfo.name} ($userId)"
        } else {
            ai.loadLabel(pm)
        }
        pkg.text = ai.packageName
        switchWidget.isChecked = AuthorizationManager.granted(packageName, uid)
        root.visibility = if (ai.metaData != null && ai.metaData.getBoolean("moe.shizuku.client.V3_REQUIRES_ROOT")) View.VISIBLE else View.GONE

        loadIconJob = AppIconCache.loadIconBitmapAsync(context, ai, ai.uid / 100000, icon)
    }

    override fun onBind(payloads: List<Any>) {
        switchWidget.isChecked = AuthorizationManager.granted(packageName, uid)
    }

    override fun onRecycle() {
        if (loadIconJob?.isActive == true) {
            loadIconJob?.cancel()
        }
    }
}
