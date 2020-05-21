package moe.shizuku.manager.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuManagerSettings
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.AboutDialogBinding
import moe.shizuku.manager.databinding.HomeActivityBinding
import moe.shizuku.manager.ktx.FixedAlwaysClipToPaddingEdgeEffectFactory
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.management.appsViewModel
import moe.shizuku.manager.settings.SettingsActivity
import moe.shizuku.manager.starter.ServerLauncher
import moe.shizuku.manager.viewmodel.Status
import moe.shizuku.manager.viewmodel.viewModels
import rikka.core.ktx.unsafeLazy
import rikka.material.widget.*
import rikka.material.widget.BorderView.OnBorderVisibilityChangedListener
import rikka.recyclerview.fixEdgeEffect

abstract class HomeActivity : AppBarActivity() {

    private val mBinderReceivedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            appsModel.load()
            adapter.updateData()
        }
    }

    private val mRequestRefreshReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkServerStatus()
        }
    }

    private val homeModel by viewModels { HomeViewModel() }
    private val appsModel by appsViewModel()
    private val adapter by unsafeLazy { HomeAdapter(homeModel, appsModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.getBoolean(R.bool.is_translation_unfinished)) {
            Toast.makeText(this, getString(R.string.toast_translation_unfinished), Toast.LENGTH_LONG).show()
        }

        val binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!writeFilesCalled) {
            ServerLauncher.writeFiles(this)
            writeFilesCalled = true
        }

        homeModel.serviceStatus.observe(this) {
            if (it.status == Status.SUCCESS) {
                val status = homeModel.serviceStatus.value?.data ?: return@observe
                adapter.updateData()
                ShizukuManagerSettings.setLastLaunchMode(if (status.uid == 0) ShizukuManagerSettings.LaunchMethod.ROOT else ShizukuManagerSettings.LaunchMethod.ADB)
            }
        }
        appsModel.grantedCount.observe(this) {
            if (it.status == Status.SUCCESS) {
                adapter.updateData()
            }
        }

        if (ShizukuService.pingBinder()) {
            appsModel.load()
        }

        val recyclerView = binding.list
        recyclerView.adapter = adapter
        recyclerView.borderViewDelegate.borderVisibilityChangedListener = OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean -> appBar!!.setRaised(!top) }
        recyclerView.fixEdgeEffect(alwaysClipToPadding = false)

        val margin = resources.getDimension(R.dimen.home_margin).toInt()
        recyclerView.setInitialPadding(
                recyclerView.initialPaddingLeft + margin,
                recyclerView.initialPaddingTop + margin,
                recyclerView.initialPaddingRight + margin,
                recyclerView.initialPaddingBottom + margin
        )

        recyclerView.post {
            recyclerView.edgeEffectFactory = FixedAlwaysClipToPaddingEdgeEffectFactory(
                    recyclerView.paddingLeft - margin,
                    recyclerView.paddingTop - margin,
                    recyclerView.paddingRight - margin,
                    recyclerView.paddingBottom - margin)
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBinderReceivedReceiver, IntentFilter(AppConstants.ACTION_BINDER_RECEIVED))
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mRequestRefreshReceiver, IntentFilter(AppConstants.ACTION_REQUEST_REFRESH))
    }

    override fun onResume() {
        super.onResume()
        checkServerStatus()
    }

    private fun checkServerStatus() {
        homeModel.reload()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBinderReceivedReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRequestRefreshReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                val binding = AboutDialogBinding.inflate(LayoutInflater.from(this), null, false)
                binding.sourceCode.movementMethod = LinkMovementMethod.getInstance()
                binding.sourceCode.text = getString(R.string.about_view_source_code, "<b><a href=\"https://github.com/RikkaApps/Shizuku\">GitHub</a></b>").toHtml()
                binding.iconCredits.movementMethod = LinkMovementMethod.getInstance()
                binding.iconCredits.text = getString(R.string.about_icon_credits, "<b><a href=\"%2\$s\">%1\$s</a></b>".format(getString(R.string.icon_illustrator), getString(R.string.icon_illustrator_url))).toHtml()

                AlertDialog.Builder(this)
                        .setView(binding.root)
                        .show()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private var writeFilesCalled = false
    }
}