package moe.shizuku.manager.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.manager.R
import moe.shizuku.manager.cmd.TerminalTutorialActivity
import moe.shizuku.manager.databinding.HomeTerminalBinding
import moe.shizuku.manager.model.ServiceStatus
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class TerminalViewHolder(private val binding: HomeTerminalBinding) : BaseViewHolder<ServiceStatus>(binding.root), View.OnClickListener {

    companion object {
        val CREATOR = Creator<ServiceStatus> { inflater: LayoutInflater, parent: ViewGroup? -> TerminalViewHolder(HomeTerminalBinding.inflate(inflater, parent, false)) }
    }

    init {
        itemView.setOnClickListener(this)
    }

    private inline val summary get() = binding.text2

    override fun onBind() {
        val context = itemView.context
        if (!data.isRunning) {
            itemView.isEnabled = false
            summary.text = context.getString(R.string.home_status_service_not_running, context.getString(R.string.app_name))
        } else {
            itemView.isEnabled = true
            summary.text = context.getString(R.string.home_terminal_description)
        }
    }

    override fun onClick(v: View) {
        v.context.startActivity(Intent(v.context, TerminalTutorialActivity::class.java))
    }
}