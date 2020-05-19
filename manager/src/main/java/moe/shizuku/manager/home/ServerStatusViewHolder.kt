package moe.shizuku.manager.home

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.ShizukuClientHelper
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.R
import moe.shizuku.manager.model.ServiceStatus
import moe.shizuku.manager.widget.MaterialCircleIconView
import rikka.html.text.HtmlCompat
import rikka.html.widget.HtmlCompatTextView
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ServerStatusViewHolder(itemView: View) : BaseViewHolder<ServiceStatus?>(itemView), View.OnClickListener {

    companion object {
        val CREATOR = Creator<ServiceStatus> { inflater: LayoutInflater, parent: ViewGroup? -> ServerStatusViewHolder(inflater.inflate(R.layout.item_home_server_status, parent, false)) }
    }

    private val textView: HtmlCompatTextView = itemView.findViewById(android.R.id.text1)
    private val summaryView: HtmlCompatTextView = itemView.findViewById(android.R.id.text2)
    private val iconView: MaterialCircleIconView = itemView.findViewById(android.R.id.icon)

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        LocalBroadcastManager.getInstance(v.context)
                .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
    }

    override fun onBind() {
        val context = itemView.context
        val status = data
        val ok = status!!.isRunning
        val isRoot = status.uid == 0
        val version = status.version
        if (ok) {
            iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_24dp))
            iconView.colorName = "blue"
        } else {
            iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_error_24dp))
            iconView.colorName = "blue_grey"
        }
        val title: String
        val summary: String
        val name = context.getString(R.string.service_name)
        var user = if (isRoot) "root" else "adb"
        if (isRoot) {
            user += if (status.seContext != null) " (context=" + status.seContext + ")" else ""
        }
        if (ok) {
            title = context.getString(R.string.service_running, name)
            summary = if (status.version != ShizukuClientHelper.getLatestVersion()) {
                context.getString(R.string.service_version_update, user, version, ShizukuClientHelper.getLatestVersion())
            } else {
                context.getString(R.string.service_version, user, version)
            }
        } else {
            title = context.getString(R.string.service_not_running_tap_retry, name)
            summary = ""
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