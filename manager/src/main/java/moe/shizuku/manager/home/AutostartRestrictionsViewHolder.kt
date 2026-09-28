package moe.shizuku.manager.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.manager.adb.AutostartRestrictions
import moe.shizuku.manager.databinding.HomeAutostartRestrictionsBinding
import moe.shizuku.manager.databinding.HomeItemContainerBinding
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class AutostartRestrictionsViewHolder(
    private val binding: HomeAutostartRestrictionsBinding,
    root: View
) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeAutostartRestrictionsBinding.inflate(inflater, outer.root, true)
            AutostartRestrictionsViewHolder(inner, outer.root)
        }
    }

    init {
        binding.buttonBattery.setOnClickListener { v ->
            AutostartRestrictions.requestIgnoreBatteryOptimizations(v.context)
        }
        binding.buttonMiui.setOnClickListener { v ->
            AutostartRestrictions.openMiuiAutostartSettings(v.context)
        }
        binding.buttonDismiss.setOnClickListener {
            AutostartRestrictions.setDismissed(true)
            (bindingAdapter as? HomeAdapter)?.updateData() ?: run {
                itemView.visibility = View.GONE
            }
        }
    }

    override fun onBind() {
        val ctx = itemView.context
        itemView.visibility = View.VISIBLE
        binding.buttonBattery.visibility =
            if (AutostartRestrictions.isBatteryOptimizationIgnored(ctx)) View.GONE else View.VISIBLE
        binding.buttonMiui.visibility =
            if (AutostartRestrictions.isXiaomi()) View.VISIBLE else View.GONE
    }
}
