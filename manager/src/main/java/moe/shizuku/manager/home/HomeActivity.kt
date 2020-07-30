package moe.shizuku.manager.home

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import moe.shizuku.api.ShizukuProvider
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.databinding.AboutDialogBinding
import moe.shizuku.manager.databinding.HomeActivityBinding
import moe.shizuku.manager.ktx.FixedAlwaysClipToPaddingEdgeEffectFactory
import moe.shizuku.manager.ktx.logd
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
    }

    private val binderDeadListener = ShizukuProvider.OnBinderDeadListener {
        checkServerStatus()
    }

    private val homeModel by viewModels { HomeViewModel() }
    private val appsModel by appsViewModel()
    private val adapter by unsafeLazy { HomeAdapter(homeModel, appsModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Starter.writeFilesAsync(this)

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
        ShizukuProvider.addBinderDeadListener(binderDeadListener)

        logd("onCreate")
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
        ShizukuProvider.removeBinderDeadListener(binderDeadListener)
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
                            try {
                                ShizukuService.exit()
                            } catch (e: Throwable) {
                            }
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