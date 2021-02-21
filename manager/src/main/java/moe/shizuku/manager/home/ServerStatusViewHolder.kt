package moe.shizuku.manager.home

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.HomeServerStatusBinding
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.widget.MaterialCircleIconView
import moe.shizuku.server.ServerConstants
import rikka.html.text.HtmlCompat
import rikka.html.widget.HtmlCompatTextView
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class ServerStatusViewHolder(private val binding: HomeServerStatusBinding) : BaseViewHolder<ServiceStatus>(binding.root) {

    companion object {
        val CREATOR = Creator<ServiceStatus> { inflater: LayoutInflater, parent: ViewGroup? -> ServerStatusViewHolder(HomeServerStatusBinding.inflate(inflater, parent, false)) }
    }

    private inline val textView: HtmlCompatTextView get() = binding.text1
    private inline val summaryView: HtmlCompatTextView get() = binding.text2
    private inline val iconView: MaterialCircleIconView get() = binding.icon

    override fun onBind() {
        val context = itemView.context
        val status = data
        val ok = status.isRunning
        val isRoot = status.uid == 0
        val apiVersion = status.apiVersion
        val patchVersion = status.patchVersion
        if (ok) {
            iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_24dp))
            iconView.colorName = "blue"
        } else {
            iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_error_24dp))
            iconView.colorName = "blue_grey"
        }
        val user = if (isRoot) "root" else "adb"
        val title = if (ok) {
            context.getString(R.string.home_status_service_is_running, context.getString(R.string.app_name))
        } else {
            context.getString(R.string.home_status_service_not_running, context.getString(R.string.app_name))
        }
        val summary = if (ok) {
            if (apiVersion != Shizuku.getLatestServiceVersion() || status.patchVersion != ServerConstants.PATCH_VERSION) {
                context.getString(R.string.home_status_service_version_update, user,
                        "${apiVersion}.${patchVersion}",
                        "${Shizuku.getLatestServiceVersion()}.${ServerConstants.PATCH_VERSION}")
            } else {
                context.getString(R.string.home_status_service_version, user, "${apiVersion}.${patchVersion}")
            }
        } else {
            ""
        }
        textView.setHtmlText(String.format("<font face=\"sans-serif-medium\">%1\$s</font>", title), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
        summaryView.setHtmlText(String.format("%1\$s", summary), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
        if (TextUtils.isEmpty(summaryView.text)) {
            summaryView.visibility = View.GONE
        } else {
            summaryView.visibility = View.VISIBLE
        }
    }
}