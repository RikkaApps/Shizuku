package moe.shizuku.manager

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.adapter.AppsAdapter
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.utils.CustomTabsHelper
import moe.shizuku.manager.viewmodel.AppsViewModel
import moe.shizuku.manager.viewmodel.DataWrapper
import moe.shizuku.manager.viewmodel.SharedViewModelProviders
import rikka.material.widget.*
import rikka.recyclerview.RecyclerViewHelper
import java.util.*

class ManageAppsActivity : AppBarActivity() {

    private val viewModel: AppsViewModel by lazy { SharedViewModelProviders.of(this).get("apps", AppsViewModel::class.java) }
    private val adapter: AppsAdapter = AppsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!ShizukuService.pingBinder() && !isFinishing) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        appBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.packages.observe(this, Observer { `object`: DataWrapper<List<PackageInfo?>?> ->
            if (isFinishing) return@Observer
            if (`object`.error == null) {
                adapter.updateData(`object`.data)
            } else {
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
                finish()
                val tr = `object`.error
                Toast.makeText(this, Objects.toString(tr, "unknown"), Toast.LENGTH_SHORT).show()
                tr.printStackTrace()
            }
        })
        if (viewModel.packages.value != null && viewModel.packages?.value?.data != null) {
            adapter.updateData(viewModel.packages?.value?.data)
        } else {
            viewModel.load(this)
        }
        val recyclerView = findViewById<BorderRecyclerView>(android.R.id.list)
        recyclerView.adapter = adapter
        RecyclerViewHelper.fixOverScroll(recyclerView)
        recyclerView.borderViewDelegate.borderVisibilityChangedListener = BorderView.OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean -> appBar?.setRaised(!top) }

        val padding = resources.getDimension(R.dimen.list_vertical_padding).toInt()
        recyclerView.setInitialPadding(
                recyclerView.initialPaddingLeft,
                recyclerView.initialPaddingTop + padding,
                recyclerView.initialPaddingRight,
                recyclerView.initialPaddingBottom + padding
        )

        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                viewModel.loadCount()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!ShizukuService.pingBinder() && !isFinishing) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
            finish()
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.apps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else if (item.itemId == R.id.action_view_apps) {
            CustomTabsHelper.launchUrlOrCopy(this, Helps.APPS.get())
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}