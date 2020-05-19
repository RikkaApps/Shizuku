package moe.shizuku.manager.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.HomeManageAppsItemBinding
import moe.shizuku.manager.management.ApplicationManagementActivity
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class ManageAppsViewHolder(private val binding: HomeManageAppsItemBinding) : BaseViewHolder<Int>(binding.root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<Int> { inflater: LayoutInflater, parent: ViewGroup? -> ManageAppsViewHolder(HomeManageAppsItemBinding.inflate(inflater, parent, false)) }
    }

    init {
        itemView.setOnClickListener(this)
    }

    private inline val title get() = binding.text1
    private inline val summary get() = binding.text2

    override fun onBind() {
        val context = itemView.context
        title.setHtmlText(context.resources.getQuantityString(R.plurals.authorized_apps_count, data!!, data))
        if (!ShizukuService.pingBinder()) {
            itemView.isEnabled = false
            summary.setHtmlText(context.getString(R.string.service_not_running, context.getString(R.string.service_name)))
        } else {
            itemView.isEnabled = true
            summary.setHtmlText(context.getString(R.string.view_authorized_apps))
        }
    }

    override fun onClick(v: View) {
        v.context.startActivity(Intent(v.context, ApplicationManagementActivity::class.java))
    }
}