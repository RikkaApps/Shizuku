package moe.shizuku.manager.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.manager.Helps
import moe.shizuku.manager.databinding.HomeLearnMoreBinding
import moe.shizuku.manager.utils.CustomTabsHelper
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class LearnMoreViewHolder(binding: HomeLearnMoreBinding) : BaseViewHolder<Any?>(binding.root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? -> LearnMoreViewHolder(HomeLearnMoreBinding.inflate(inflater, parent, false)) }
    }

    init {
        binding.root.setOnClickListener { v: View -> CustomTabsHelper.launchUrlOrCopy(v.context, Helps.HOME.get()) }
    }
}