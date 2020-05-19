package moe.shizuku.manager.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuManagerSettings
import moe.shizuku.manager.databinding.HomeStartAdbBinding
import moe.shizuku.manager.starter.ServerLauncher
import moe.shizuku.manager.utils.CustomTabsHelper
import rikka.core.util.ClipboardUtils
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class StartAdbViewHolder(private val binding: HomeStartAdbBinding) : BaseViewHolder<Any?>(binding.root), View.OnClickListener, Checkable {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? -> StartAdbViewHolder(HomeStartAdbBinding.inflate(inflater, parent, false)) }
    }

    private inline val expandableButton get() = binding.text2
    private inline val expandableLayout get() = binding.expandable

    init {
        expandableButton.setOnClickListener(this)
        binding.button1.setOnClickListener { v: View -> CustomTabsHelper.launchUrlOrCopy(v.context, Helps.ADB.get()) }
        binding.button2.setOnClickListener { v: View ->
            val context = v.context
            AlertDialog.Builder(context)
                    .setTitle(R.string.view_command)
                    .setMessage(HtmlCompat.fromHtml(context.getString(R.string.view_command_message, ServerLauncher.getCommandAdb())))
                    .setPositiveButton(R.string.copy_command) { _, _ ->
                        if (ClipboardUtils.put(context, ServerLauncher.getCommandAdb())) {
                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.getCommandAdb()), Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.send_command) { _, _ ->
                        var intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_TEXT, ServerLauncher.getCommandAdb())
                        intent = Intent.createChooser(intent, context.getString(R.string.send_command))
                        context.startActivity(intent)
                    }
                    .show()
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
        ShizukuManagerSettings.getPreferences().edit().putBoolean("adb_help_expanded", checked).apply()
    }

    override fun isChecked(): Boolean {
        return ShizukuManagerSettings.getPreferences().getBoolean("adb_help_expanded", true)
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun syncViewState() {
        expandableButton.isChecked = isChecked
        expandableLayout.isExpanded = isChecked
    }
}