package moe.shizuku.manager.home

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.appcompat.app.AlertDialog
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.databinding.HomeStartRootBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.starter.ShellService
import moe.shizuku.manager.starter.StarterActivity
import moe.shizuku.manager.utils.BindServiceHelper
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
        binding.text1.text = context.getString(R.string.home_root_description, "<b><a href=\"https://dontkillmyapp.com/\">Don\'t kill my app!</a></b>").toHtml(HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE)
    }

    private fun onStartClicked(v: View) {
        val context = v.context
        val intent = Intent(context, StarterActivity::class.java).apply {
            putExtra(StarterActivity.EXTRA_IS_ROOT, true)
        }
        context.startActivity(intent)
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