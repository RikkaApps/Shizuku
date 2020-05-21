package moe.shizuku.manager.settings

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuManagerSettings
import moe.shizuku.manager.app.ThemeHelper.KEY_BLACK_NIGHT_THEME
import moe.shizuku.manager.utils.CustomTabsHelper
import moe.shizuku.preference.*
import rikka.core.util.ResourceUtils
import rikka.html.text.HtmlCompat
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate
import rikka.material.widget.BorderRecyclerView
import rikka.material.widget.BorderView
import rikka.recyclerview.addVerticalPadding
import rikka.recyclerview.fixEdgeEffect
import java.util.*
import moe.shizuku.manager.ShizukuManagerSettings.KEEP_SU_CONTEXT as KEY_KEEP_SU_CONTEXT
import moe.shizuku.manager.ShizukuManagerSettings.LANGUAGE as KEY_LANGUAGE
import moe.shizuku.manager.ShizukuManagerSettings.NIGHT_MODE as KEY_NIGHT_MODE

class SettingsFragment : PreferenceFragment() {

    companion object {
        init {
            SimpleMenuPreference.setLightFixEnabled(true)
        }
    }

    private lateinit var languagePreference: ListPreference
    private lateinit var nightModePreference: Preference
    private lateinit var blackNightThemePreference: SwitchPreference
    private lateinit var keepSuContextPreference: SwitchPreference
    private lateinit var startupPreference: PreferenceCategory
    private lateinit var translationPreference: Preference
    private lateinit var translationContributorsPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()

        preferenceManager.setStorageDeviceProtected()
        preferenceManager.sharedPreferencesName = ShizukuManagerSettings.NAME
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.settings, null)

        languagePreference = findPreference(KEY_LANGUAGE) as ListPreference
        nightModePreference = findPreference(KEY_NIGHT_MODE)
        blackNightThemePreference = findPreference(KEY_BLACK_NIGHT_THEME) as SwitchPreference
        keepSuContextPreference = findPreference(KEY_KEEP_SU_CONTEXT) as SwitchPreference
        startupPreference = findPreference("startup") as PreferenceCategory
        translationPreference = findPreference("translation")
        translationContributorsPreference = findPreference("translation_contributors")

        keepSuContextPreference.isVisible = false
        startupPreference.isVisible = false

        languagePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (newValue is String) {
                val locale: Locale = if ("SYSTEM" == newValue) {
                    LocaleDelegate.systemLocale
                } else {
                    Locale.forLanguageTag(newValue)
                }
                LocaleDelegate.defaultLocale = locale
                activity?.recreate()
            }
            true
        }

        val tag = languagePreference.value
        val index = listOf(*languagePreference.entryValues).indexOf(tag)
        val localeName: MutableList<String> = ArrayList()
        val localeNameUser: MutableList<String> = ArrayList()
        val userLocale = ShizukuManagerSettings.getLocale()
        for (i in 1 until languagePreference.entries.size) {
            val locale = Locale.forLanguageTag(languagePreference.entries[i].toString())
            localeName.add(if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(locale) else locale.getDisplayName(locale))
            localeNameUser.add(if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(userLocale) else locale.getDisplayName(userLocale))
        }

        for (i in 1 until languagePreference.entries.size) {
            if (index != i) {
                languagePreference.entries[i] = HtmlCompat.fromHtml(String.format("%s - %s",
                        localeName[i - 1],
                        localeNameUser[i - 1]
                ))
            } else {
                languagePreference.entries[i] = localeNameUser[i - 1]
            }
        }

        if (TextUtils.isEmpty(tag) || "SYSTEM" == tag) {
            languagePreference.summary = getString(R.string.follow_system)
        } else if (index != -1) {
            val name = localeNameUser[index - 1]
            languagePreference.summary = name
        }
        nightModePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value is Int) {
                if (ShizukuManagerSettings.getNightMode() != value) {
                    DayNightDelegate.setDefaultNightMode(value)
                    activity?.recreate()
                }
            }
            true
        }
        blackNightThemePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
            if (ResourceUtils.isNightMode(requireContext().resources.configuration)) activity?.recreate()
            true
        }

        translationPreference.summary = context.getString(R.string.settings_translation_summary, context.getString(R.string.app_name))
        translationPreference.setOnPreferenceClickListener {
            CustomTabsHelper.launchUrlOrCopy(context, context.getString(R.string.translation_url))
            true
        }

        if (context.resources.getBoolean(R.bool.show_translation_contributors)) {
            translationContributorsPreference.summary = context.getString(R.string.translation_contributors)
        } else {
            translationContributorsPreference.isVisible = false
        }
    }

    override fun onCreateItemDecoration(): DividerDecoration? {
        return CategoryDivideDividerDecoration()
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState) as BorderRecyclerView
        recyclerView.fixEdgeEffect()
        recyclerView.addVerticalPadding(0, 8)

        val lp = recyclerView.layoutParams
        if (lp is FrameLayout.LayoutParams) {
            lp.rightMargin = recyclerView.context.resources.getDimension(R.dimen.rd_activity_horizontal_margin).toInt()
            lp.leftMargin = lp.rightMargin
        }

        recyclerView.borderViewDelegate.borderVisibilityChangedListener = BorderView.OnBorderVisibilityChangedListener { top: Boolean, _: Boolean, _: Boolean, _: Boolean -> (activity as SettingsActivity?)?.appBar?.setRaised(!top) }
        return recyclerView
    }
}