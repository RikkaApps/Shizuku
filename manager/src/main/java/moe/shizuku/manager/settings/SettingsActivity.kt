package moe.shizuku.manager.settings

import android.os.Bundle
import android.view.MenuItem
import moe.shizuku.manager.R
import moe.shizuku.manager.starter.ServerLauncher
import moe.shizuku.manager.ShizukuManagerSettings
import moe.shizuku.manager.app.AppBarFragmentActivity

class SettingsActivity : AppBarFragmentActivity() {

    private var isStartServiceV2 = false
    private var isKeepSuContext = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()
            isStartServiceV2 = ShizukuManagerSettings.isStartServiceV2()
            isKeepSuContext = ShizukuManagerSettings.isKeepSuContext()
        } else {
            isStartServiceV2 = savedInstanceState.getBoolean("start_v2", false)
            isKeepSuContext = savedInstanceState.getBoolean("keep_su_context", true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("start_v2", isStartServiceV2)
        outState.putBoolean("keep_su_context", isKeepSuContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ShizukuManagerSettings.isStartServiceV2() != isStartServiceV2
                || ShizukuManagerSettings.isKeepSuContext() != isKeepSuContext) {
            ServerLauncher.writeFiles(this, true)
            if (ServerLauncher.COMMAND_ROOT != null) {
                ServerLauncher.writeFiles(this, false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}