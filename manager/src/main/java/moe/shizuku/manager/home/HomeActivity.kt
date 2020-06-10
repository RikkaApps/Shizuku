package moe.shizuku.manager.home

import android.content.*
import android.os.Bundle
import android.os.Parcel
import android.os.Process
import android.os.UserHandle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuProvider
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.AboutDialogBinding
import moe.shizuku.manager.databinding.HomeActivityBinding
import moe.shizuku.manager.ktx.FixedAlwaysClipToPaddingEdgeEffectFactory
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.management.appsViewModel
import moe.shizuku.manager.settings.SettingsActivity
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.utils.AppIconCache
import moe.shizuku.manager.viewmodel.Status
import moe.shizuku.manager.viewmodel.viewModels
import rikka.core.ktx.unsafeLazy
import rikka.material.widget.*
import rikka.material.widget.BorderView.OnBorderVisibilityChangedListener
import rikka.recyclerview.fixEdgeEffect

abstract class HomeActivity : AppBarActivity() {

    private val binderReceivedListener = ShizukuProvider.OnBinderReceivedListener {
        checkServerStatus()
        appsModel.load()
        adapter.updateData()
    }

    private val requestRefreshReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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

        Starter.writeFiles(this)

        homeModel.serviceStatus.observe(this) {
            if (it.status == Status.SUCCESS) {
                val status = homeModel.serviceStatus.value?.data ?: return@observe
                adapter.updateData()
                ShizukuSettings.setLastLaunchMode(if (status.uid == 0) ShizukuSettings.LaunchMethod.ROOT else ShizukuSettings.LaunchMethod.ADB)
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

        ShizukuProvider.addBinderReceivedListenerSticky(binderReceivedListener)

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(requestRefreshReceiver, IntentFilter(AppConstants.ACTION_REQUEST_REFRESH))
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
        ShizukuProvider.removeBinderReceivedListener(binderReceivedListener)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestRefreshReceiver)
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
                binding.icon.setImageBitmap(AppIconCache.getOrLoadBitmap(this, applicationInfo, Process.myUid() / 100000, resources.getDimensionPixelOffset(R.dimen.default_app_icon_size)))
                AlertDialog.Builder(this)
                        .setView(binding.root)
                        .show()
                true
            }
            R.id.action_stop -> {
                if (!ShizukuService.pingBinder()) {
                    return true
                }
                AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_stop_message)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            val data = Parcel.obtain()
                            val reply = Parcel.obtain()
                            try {
                                data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR)
                                ShizukuService.getBinder()?.transact(101, data, reply, 0)
                                reply.readException()
                            } catch (ignored: Throwable) {
                            } finally {
                                data.recycle()
                                reply.recycle()
                            }
                            LocalBroadcastManager.getInstance(this)
                                    .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
                        }
                        .setNegativeButton(android.R.string.cancel, null)
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
}