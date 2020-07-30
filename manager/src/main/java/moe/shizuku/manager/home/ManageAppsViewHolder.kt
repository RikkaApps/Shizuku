package moe.shizuku.manager.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.HomeManageAppsItemBinding
import moe.shizuku.manager.management.ApplicationManagementActivity
import moe.shizuku.manager.model.ServiceStatus
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ManageAppsViewHolder(private val binding: HomeManageAppsItemBinding) : BaseViewHolder<Pair<ServiceStatus, Int>>(binding.root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Pair<ServiceStatus, Int>> { inflater: LayoutInflater, parent: ViewGroup? -> ManageAppsViewHolder(HomeManageAppsItemBinding.inflate(inflater, parent, false)) }
    }

    init {
        itemView.setOnClickListener(this)
    }

    private inline val title get() = binding.text1
    private inline val summary get() = binding.text2

    override fun onBind() {
        val context = itemView.context
        if (!data.first.isRunning) {
            itemView.isEnabled = false
            title.setText(R.string.home_app_management_title)
            summary.setHtmlText(context.getString(R.string.home_status_service_not_running, context.getString(R.string.app_name)))
        } else {
            itemView.isEnabled = true
            title.setHtmlText(context.resources.getQuantityString(R.plurals.home_app_management_authorized_apps_count, data.second, data.second))
            summary.setHtmlText(context.getString(R.string.home_app_management_view_authorized_apps))
        }
    }

    override fun onClick(v: View) {
        v.context.startActivity(Intent(v.context, ApplicationManagementActivity::class.java))
    }
}