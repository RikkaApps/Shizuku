package moe.shizuku.manager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import moe.shizuku.api.ShizukuService
import moe.shizuku.manager.adapter.AppsAdapter
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.utils.CustomTabsHelper
import moe.shizuku.manager.viewmodel.AppsViewModel
import moe.shizuku.manager.viewmodel.SharedViewModelProviders
import moe.shizuku.manager.viewmodel.Status
import rikka.material.widget.*
import rikka.recyclerview.RecyclerViewHelper
import java.util.*

class ManageAppsActivity : AppBarActivity() {

    private val viewModel: AppsViewModel by lazy { SharedViewModelProviders.of(this).get(AppsViewModel::class.java) }
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

        viewModel.packages.observe(this) {
            when (it?.status) {
                Status.SUCCESS -> {
                    adapter.updateData(it.data)
                }
                Status.ERROR -> {
                    LocalBroadcastManager.getInstance(this)
                            .sendBroadcast(Intent(AppConstants.ACTION_REQUEST_REFRESH))
                    finish()
                    val tr = it.error
                    Toast.makeText(this, Objects.toString(tr, "unknown"), Toast.LENGTH_SHORT).show()
                    tr.printStackTrace()
                }
                Status.LOADING -> {

                }
            }
        }
        if (viewModel.packages.value == null) {
            viewModel.load()
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