package moe.shizuku.manager

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.adapter.HomeAdapter
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.viewmodel.AppsViewModel
import moe.shizuku.manager.viewmodel.HomeViewModel
import moe.shizuku.manager.viewmodel.SharedViewModelProviders
import moe.shizuku.manager.viewmodel.Status
import rikka.html.text.HtmlCompat
import rikka.material.widget.*
import rikka.material.widget.BorderView.OnBorderVisibilityChangedListener
import rikka.recyclerview.RecyclerViewHelper

class MainActivity : AppBarActivity() {

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

    private val homeModel by viewModels<HomeViewModel>()
    private val appsModel by lazy { SharedViewModelProviders.of(this).get(AppsViewModel::class.java) }
    private val adapter: HomeAdapter by lazy { HomeAdapter(homeModel, appsModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!writeFilesCalled) {
            ServerLauncher.writeFiles(this, true)
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

        val recyclerView = findViewById<BorderRecyclerView>(android.R.id.list)
        recyclerView.adapter = adapter
        recyclerView.borderViewDelegate.borderVisibilityChangedListener = OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean -> appBar!!.setRaised(!top) }
        RecyclerViewHelper.fixOverScroll(recyclerView)

        val margin = resources.getDimension(R.dimen.home_margin).toInt()
        recyclerView.setInitialPadding(
                recyclerView.initialPaddingLeft + margin,
                recyclerView.initialPaddingTop + margin,
                recyclerView.initialPaddingRight + margin,
                recyclerView.initialPaddingBottom + margin
        )

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
                val dialog: Dialog = AlertDialog.Builder(this)
                        .setView(R.layout.dialog_about)
                        .show()
                (dialog.findViewById<View>(R.id.source_code) as TextView).movementMethod = LinkMovementMethod.getInstance()
                (dialog.findViewById<View>(R.id.icon_credits) as TextView).movementMethod = LinkMovementMethod.getInstance()
                (dialog.findViewById<View>(R.id.icon_credits) as TextView).text = HtmlCompat.fromHtml(getString(R.string.about_icon_credits))
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