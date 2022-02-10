package moe.shizuku.manager.home

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.HomeItemContainerBinding
import moe.shizuku.manager.databinding.HomeStartAdbBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.starter.Starter
import rikka.core.util.ClipboardUtils
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class StartAdbViewHolder(binding: HomeStartAdbBinding, root: View) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeStartAdbBinding.inflate(inflater, outer.root, true)
            StartAdbViewHolder(inner, outer.root)
        }
    }

    init {
        binding.button1.setOnClickListener { v: View ->
            val context = v.context
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.home_adb_button_view_command)
                .setMessage(
                    HtmlCompat.fromHtml(
                        context.getString(
                            R.string.home_adb_dialog_view_command_message,
                            Starter.adbCommand
                        )
                    )
                )
                .setPositiveButton(R.string.home_adb_dialog_view_command_copy_button) { _, _ ->
                    if (ClipboardUtils.put(context, Starter.adbCommand)) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_copied_to_clipboard, Starter.adbCommand),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.home_adb_dialog_view_command_button_send) { _, _ ->
                    var intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, Starter.adbCommand)
                    intent = Intent.createChooser(
                        intent,
                        context.getString(R.string.home_adb_dialog_view_command_button_send)
                    )
                    context.startActivity(intent)
                }
                .show()
        }
        binding.text1.movementMethod = LinkMovementMethod.getInstance()
        binding.text1.text = context.getString(R.string.home_adb_description, Helps.ADB.get())
            .toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
    }
}
