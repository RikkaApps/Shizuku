package moe.shizuku.manager.settings

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.KEEP_START_ON_BOOT
import moe.shizuku.manager.app.ThemeHelper
import moe.shizuku.manager.app.ThemeHelper.KEY_BLACK_NIGHT_THEME
import moe.shizuku.manager.ktx.isComponentEnabled
import moe.shizuku.manager.ktx.setComponentEnabled
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.starter.BootCompleteReceiver
import moe.shizuku.manager.utils.CustomTabsHelper
import rikka.core.util.ResourceUtils
import rikka.html.text.HtmlCompat
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate
import rikka.preference.SimpleMenuPreference
import rikka.recyclerview.addVerticalPadding
import rikka.recyclerview.fixEdgeEffect
import rikka.widget.borderview.BorderRecyclerView
import rikka.widget.borderview.BorderView
import java.text.NumberFormat
import java.util.*
import moe.shizuku.manager.ShizukuSettings.LANGUAGE as KEY_LANGUAGE
import moe.shizuku.manager.ShizukuSettings.NIGHT_MODE as KEY_NIGHT_MODE

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var languagePreference: ListPreference
    private lateinit var nightModePreference: IntegerSimpleMenuPreference
    private lateinit var blackNightThemePreference: SwitchPreference
    private lateinit var startOnBootPreference: SwitchPreference
    private lateinit var startupPreference: PreferenceCategory
    private lateinit var translationPreference: Preference
    private lateinit var translationContributorsPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()

        preferenceManager.setStorageDeviceProtected()
        preferenceManager.sharedPreferencesName = ShizukuSettings.NAME
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.settings, null)

        languagePreference = findPreference(KEY_LANGUAGE)!!
        nightModePreference = findPreference(KEY_NIGHT_MODE)!!
        blackNightThemePreference = findPreference(KEY_BLACK_NIGHT_THEME)!!
        startOnBootPreference = findPreference(KEEP_START_ON_BOOT)!!
        startupPreference = findPreference("startup")!!
        translationPreference = findPreference("translation")!!
        translationContributorsPreference = findPreference("translation_contributors")!!

        val componentName = ComponentName(context.packageName, BootCompleteReceiver::class.java.name)

        startOnBootPreference.isChecked = context.packageManager.isComponentEnabled(componentName)
        startOnBootPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (newValue is Boolean) {
                context.packageManager.setComponentEnabled(componentName, newValue)
                context.packageManager.isComponentEnabled(componentName) == newValue
            } else false
        }
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
        val userLocale = ShizukuSettings.getLocale()
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
        nightModePreference.value = ShizukuSettings.getNightMode()
        nightModePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value is Int) {
                if (ShizukuSettings.getNightMode() != value) {
                    DayNightDelegate.setDefaultNightMode(value)
                    activity?.recreate()
                }
            }
            true
        }
        if (ResourceUtils.isNightMode(context.resources.configuration)) {
            blackNightThemePreference.isChecked = ThemeHelper.isBlackNightTheme(context)
            blackNightThemePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                activity?.recreate()
                true
            }
        } else {
            blackNightThemePreference.isVisible = false
        }

        translationPreference.summary = context.getString(R.string.settings_translation_summary, context.getString(R.string.app_name))
        translationPreference.setOnPreferenceClickListener {
            CustomTabsHelper.launchUrlOrCopy(context, context.getString(R.string.translation_url))
            true
        }

        val contributors = context.getString(R.string.translation_contributors).toHtml().toString()
        if (contributors.isNotBlank()) {
            translationContributorsPreference.summary = contributors
        } else {
            translationContributorsPreference.isVisible = false
        }

        val translationPercentage = resources.getInteger(R.integer.translation_percentage)
        val translationShowPercentageOverride = resources.getBoolean(R.bool.translation_show_percentage_override)
        translationPreference.summary = context.getString(R.string.settings_translation_summary, context.getString(R.string.app_name)) +
                if (translationShowPercentageOverride || translationPercentage != 100) {
                    val percent = translationPercentage.toFloat() / 100
                    val percentFormatter = NumberFormat.getPercentInstance(ShizukuSettings.getLocale())
                    val percentOut = percentFormatter.format(percent)

                    context.getString(R.string.settings_translation_summary_percentage, percentOut)
                } else {
                    ""
                }
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
