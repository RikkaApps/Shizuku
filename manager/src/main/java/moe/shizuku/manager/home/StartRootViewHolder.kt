package moe.shizuku.manager.home

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.IBinder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.databinding.HomeStartRootBinding
import moe.shizuku.manager.databinding.ShellDialogBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.starter.ServerLauncher
import moe.shizuku.manager.starter.ShellService
import moe.shizuku.manager.starter.ShellService.ShellServiceBinder
import moe.shizuku.manager.utils.BindServiceHelper
import rikka.core.util.ContextUtils
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class StartRootViewHolder(private val binding: HomeStartRootBinding) : BaseViewHolder<Boolean>(binding.root), View.OnClickListener, Checkable {

    companion object {
        val CREATOR = Creator<Boolean> { inflater: LayoutInflater, parent: ViewGroup? -> StartRootViewHolder(HomeStartRootBinding.inflate(inflater, parent, false)) }
    }

    private inline val expandableButton get() = binding.text2
    private inline val expandableLayout get() = binding.expandable
    private inline val start get() = binding.button1
    private inline val restart get() = binding.button2

    private var alertDialog: AlertDialog? = null
    private val bindServiceHelper: BindServiceHelper

    init {
        expandableButton.setOnClickListener(this)
        bindServiceHelper = BindServiceHelper(itemView.context, ShellService::class.java)
        val listener = View.OnClickListener { v: View -> onStartClicked(v) }
        start.setOnClickListener(listener)
        restart.setOnClickListener(listener)
        binding.text1.movementMethod = LinkMovementMethod.getInstance()
        binding.text1.text = binding.text1.context.getString(R.string.home_root_description, "<b><a href=\"https://dontkillmyapp.com/\">Don\'t kill my app!</a></b>").toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
    }

    private fun onStartClicked(v: View) {
        startServer(v.context)
    }

    private fun startShell(context: Context, vararg command: String) {
        start.isEnabled = false
        restart.isEnabled = false
        val sb = StringBuilder()
        val binding = ShellDialogBinding.inflate(LayoutInflater.from(context), null, false)
        val alertDialog = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.home_adb_dialog_view_command_button_send) { dialog: DialogInterface?, which: Int ->
                    val activity = ContextUtils.getActivity<Activity>(context) ?: return@setNeutralButton
                    ShareCompat.IntentBuilder.from(activity)
                            .setText(sb.toString())
                            .setType("text/plain")
                            .setChooserTitle(R.string.home_adb_dialog_view_command_button_send)
                            .startChooser()
                }
                .show()
        this.alertDialog = alertDialog

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).visibility = View.GONE

        val textView = binding.text1
        textView.setText(R.string.starting_shell)

        bindServiceHelper.bind { binder: IBinder ->
            val service = binder as ShellServiceBinder
            service.run(command, object : ShellService.Listener {
                override fun onFailed() {
                    bindServiceHelper.unbind()
                    if (alertDialog == null) {
                        return
                    }
                    start.isEnabled = true
                    restart.isEnabled = true
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    textView.setText(R.string.start_with_root_failed)
                }

                override fun onCommandResult(exitCode: Int) {
                    bindServiceHelper.unbind()
                    if (alertDialog == null) {
                        return
                    }
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    if (exitCode != 0) {
                        sb.append('\n').append("Send this to developer may help solve the problem.")
                        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).visibility = View.VISIBLE
                        start.isEnabled = true
                        restart.isEnabled = true
                    }
                }

                override fun onLine(line: String) {
                    if (alertDialog == null) {
                        return
                    }
                    if (sb.isNotEmpty()) {
                        sb.append('\n')
                    }
                    sb.append(line)
                    textView.text = sb.toString()
                }
            })
        }
    }

    private fun startServer(context: Context) {
        start.isEnabled = false
        restart.isEnabled = false
        if (ServerLauncher.getCommand() == null) {
            ServerLauncher.writeFiles(context)
        }
        startShell(context, ServerLauncher.getCommand())
    }

    override fun onBind() {
        start.isEnabled = true
        restart.isEnabled = true
        if (data!!) {
            start.visibility = View.GONE
            restart.visibility = View.VISIBLE
        } else {
            start.visibility = View.VISIBLE
            restart.visibility = View.GONE
        }
        syncViewState()
    }

    override fun onRecycle() {
        super.onRecycle()
        alertDialog = null
    }

    override fun onClick(v: View) {
        isChecked = !isChecked
        syncViewState()
    }

    override fun setChecked(checked: Boolean) {
        ShizukuSettings.getPreferences().edit().putBoolean("root_help_expanded", checked).apply()
    }

    override fun isChecked(): Boolean {
        return ShizukuSettings.getPreferences().getBoolean("root_help_expanded", true)
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun syncViewState() {
        expandableButton.isChecked = isChecked
        expandableLayout.isExpanded = isChecked
    }
}