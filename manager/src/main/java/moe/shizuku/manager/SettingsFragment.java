package moe.shizuku.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import moe.shizuku.manager.app.ThemeHelper;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.SharedViewModelProviders;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.SwitchPreference;
import moe.shizuku.support.app.DayNightDelegate;
import moe.shizuku.support.app.LocaleDelegate;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;
import moe.shizuku.support.utils.HtmlUtils;
import moe.shizuku.support.utils.ResourceUtils;

public class SettingsFragment extends PreferenceFragment {

    public static final String KEY_LANGUAGE = ShizukuManagerSettings.LANGUAGE;
    public static final String KEY_NIGHT_MODE = ShizukuManagerSettings.NIGHT_MODE;
    public static final String KEY_BLACK_NIGHT_THEME = ThemeHelper.KEY_BLACK_NIGHT_THEME;
    public static final String KEY_NO_V2 = ShizukuManagerSettings.NO_V2;

    private ListPreference languagePreference;
    private Preference nightModePreference;
    private SwitchPreference blackNightThemePreference;
    private SwitchPreference noV2Preference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        getPreferenceManager().setSharedPreferencesName(ShizukuManagerSettings.NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        setPreferencesFromResource(R.xml.settings, null);

        languagePreference = (ListPreference) findPreference(KEY_LANGUAGE);
        nightModePreference = findPreference(KEY_NIGHT_MODE);
        blackNightThemePreference = (SwitchPreference) findPreference(KEY_BLACK_NIGHT_THEME);
        noV2Preference = (SwitchPreference) findPreference(KEY_NO_V2);

        AppsViewModel viewModel = SharedViewModelProviders.of(this).get("apps", AppsViewModel.class);
        viewModel.observe(this, object -> {
            if (object != null && !(object instanceof Throwable)) {
                if (object instanceof List) {
                    //noinspection unchecked
                    updateData((List<PackageInfo>) object);
                }
            }
        });
        if (viewModel.getData() != null) {
            updateData(viewModel.getData());
        }

        languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof String) {
                Locale locale;
                if ("SYSTEM".equals(newValue)) {
                    locale = LocaleDelegate.getSystemLocale();
                } else {
                    locale = Locale.forLanguageTag((String) newValue);
                }
                LocaleDelegate.setDefaultLocale(locale);
                recreateActivity();
            }
            return true;
        });

        for (int i = 1; i < languagePreference.getEntryValues().length; i++) {
            Locale locale = Locale.forLanguageTag(languagePreference.getEntryValues()[i].toString());
            languagePreference.getEntries()[i] = HtmlUtils.fromHtml(
                    String.format(Locale.getDefault(), "%s<br><small>%s</small>",
                            locale.getDisplayName(locale),
                            locale.getDisplayName(ShizukuManagerSettings.getLocale())
                    ));
        }

        String tag = languagePreference.getValue();
        if (!TextUtils.isEmpty(tag) && !"SYSTEM".equals(tag)) {
            Locale locale = Locale.forLanguageTag(tag);
            languagePreference.setSummary(locale.getDisplayLanguage(locale));
        } else {
            languagePreference.setSummary(getString(R.string.night_mode_follow_system));
        }

        nightModePreference.setOnPreferenceChangeListener((preference, o) -> {
            if (o instanceof Integer) {
                int mode = (int) o;
                if (ShizukuManagerSettings.getNightMode() != mode) {
                    DayNightDelegate.setDefaultNightMode(mode);
                    recreateActivity();
                }
            }
            return true;
        });

        blackNightThemePreference.setOnPreferenceChangeListener(((preference, newValue) -> {
            if (ResourceUtils.isNightMode(requireContext().getResources().getConfiguration()))
                recreateActivity();
            return true;
        }));
    }

    @Nullable
    @Override
    public DividerDecoration onCreateItemDecoration() {
        return new CategoryDivideDividerDecoration();
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        RecyclerViewHelper.fixOverScroll(recyclerView);

        int padding = (int) (8 * recyclerView.getContext().getResources().getDisplayMetrics().density);
        recyclerView.setPaddingRelative(recyclerView.getPaddingStart(), padding, recyclerView.getPaddingEnd(), padding);
        return recyclerView;
    }

    private void recreateActivity() {
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void updateData(List<PackageInfo> data) {
        int count = 0;
        for (PackageInfo pi : data) {
            ApplicationInfo ai = pi.applicationInfo;
            if (ai.metaData == null || !ai.metaData.getBoolean("moe.shizuku.client.V3_SUPPORT")) {
                count++;
            }
        }

        noV2Preference.setSummary(requireContext().getResources().getQuantityString(R.plurals.start_v2_service_summary, count, count));
    }
}
