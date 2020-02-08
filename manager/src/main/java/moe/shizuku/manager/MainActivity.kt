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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.adapter.HomeAdapter
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.viewmodel.AppsViewModel
import moe.shizuku.manager.viewmodel.DataWrapper
import moe.shizuku.manager.viewmodel.HomeViewModel
import moe.shizuku.manager.viewmodel.SharedViewModelProviders
import rikka.html.text.HtmlCompat
import rikka.material.widget.*
import rikka.material.widget.BorderView.OnBorderVisibilityChangedListener
import rikka.recyclerview.RecyclerViewHelper

class MainActivity : AppBarActivity() {

    private val mBinderReceivedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            appsModel!!.load(context)
            adapter!!.updateData(context)
        }
    }
    private val mRequestRefreshReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkServerStatus()
        }
    }

    private val homeModel by viewModels<HomeViewModel>()
    private var appsModel: AppsViewModel? = null
    private var adapter: HomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!writeFilesCalled) {
            ServerLauncher.writeFiles(this, true)
            writeFilesCalled = true
        }
        homeModel.observe(this, Observer {
            if (isFinishing) return@Observer
            if (it != null && it !is Throwable) {
                val context: Context = this
                adapter!!.updateData(context)
                if (homeModel.serviceStatus.isV3Running) {
                    ShizukuManagerSettings.setLastLaunchMode(if (homeModel.serviceStatus.uid == 0) ShizukuManagerSettings.LaunchMethod.ROOT else ShizukuManagerSettings.LaunchMethod.ADB)
                }
            }
        })
        appsModel = SharedViewModelProviders.of(this).get("apps", AppsViewModel::class.java)
        appsModel!!.grantedCount.observe(this, Observer { `object`: DataWrapper<Int?> ->
            val context: Context = this
            if (`object`.data != null) {
                adapter!!.updateData(context)
            }
        })
        if (ShizukuService.pingBinder()) {
            appsModel!!.load(this)
        }
        adapter = HomeAdapter(this, homeModel, appsModel)
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
        homeModel.load()
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