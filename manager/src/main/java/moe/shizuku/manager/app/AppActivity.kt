package moe.shizuku.manager.app

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import moe.shizuku.fontprovider.FontProviderClient
import rikka.core.res.resolveColor
import rikka.material.app.MaterialActivity

abstract class AppActivity : MaterialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!sFontInitialized && Build.VERSION.SDK_INT < 28) {
            val client = FontProviderClient.create(this)
            client?.replace("Noto Sans CJK",
                    "sans-serif", "sans-serif-medium")
            sFontInitialized = true
        }
        super.onCreate(savedInstanceState)
    }

    override fun computeUserThemeKey(): String {
        return ThemeHelper.getTheme(this)
    }

    override fun onApplyUserThemeResource(theme: Theme, isDecorView: Boolean) {
        theme.applyStyle(ThemeHelper.getThemeStyleRes(this), true)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()

        val window = window
        val theme = theme

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window?.decorView?.post {
                if (window.decorView.rootWindowInsets?.systemWindowInsetBottom ?: 0 >= Resources.getSystem().displayMetrics.density * 40) {
                    window.navigationBarColor = theme.resolveColor(android.R.attr.navigationBarColor) and 0x00ffffff or -0x20000000
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }
                } else {
                    window.navigationBarColor = Color.TRANSPARENT
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = true
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }

    companion object {
        private var sFontInitialized = false
    }
}