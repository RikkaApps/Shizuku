package moe.shizuku.manager.home

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbPairingClient
import moe.shizuku.manager.databinding.HomeStartWirelessAdbBinding
import moe.shizuku.manager.ktx.toHtml
import rikka.core.util.BuildUtils
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class StartWirelessAdbViewHolder(private val binding: HomeStartWirelessAdbBinding) : BaseViewHolder<Any?>(binding.root), View.OnClickListener, Checkable {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? -> StartWirelessAdbViewHolder(HomeStartWirelessAdbBinding.inflate(inflater, parent, false)) }
    }

    private inline val expandableButton get() = binding.text2
    private inline val expandableLayout get() = binding.expandable

    init {
        expandableButton.isVisible = false
        expandableButton.setOnClickListener(this)
        binding.button1.setOnClickListener { v: View ->
            AdbDialogFragment().show((v.context as FragmentActivity).supportFragmentManager)
        }
        binding.text1.movementMethod = LinkMovementMethod.getInstance()
        binding.text1.text = context.getString(R.string.home_wireless_adb_description, Helps.ADB_ANDROID11.get()).toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
        if (BuildUtils.atLeast30) {
            binding.button2.setOnClickListener { v: View ->
                onPairClicked(v.context)
            }
        } else {
            binding.button2.isVisible = false
        }
    }

    @SuppressLint("NewApi")
    private fun onPairClicked(context: Context) {
        if (AdbPairingClient.available()) {
            AdbPairDialogFragment().show((context as FragmentActivity).supportFragmentManager)
        } else {
            Toast.makeText(context, "Paring is not available on this device.", Toast.LENGTH_LONG).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
        }
    }

    override fun onClick(v: View) {
        isChecked = !isChecked
        syncViewState()
    }

    override fun onBind() {
        syncViewState()
    }

    override fun setChecked(checked: Boolean) {
        ShizukuSettings.getPreferences().edit().putBoolean("wireless_adb_help_expanded", checked).apply()
    }

    override fun isChecked(): Boolean {
        return true//ShizukuSettings.getPreferences().getBoolean("wireless_adb_help_expanded", true)
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun syncViewState() {
        expandableButton.isChecked = isChecked
        expandableLayout.isExpanded = isChecked
    }
}