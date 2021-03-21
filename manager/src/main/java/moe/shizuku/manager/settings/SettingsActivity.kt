package moe.shizuku.manager.settings

import android.os.Bundle
import android.view.MenuItem
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppBarFragmentActivity

class SettingsActivity : AppBarFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()
        }
    }
}