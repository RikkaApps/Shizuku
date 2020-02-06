package moe.shizuku.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import moe.shizuku.manager.app.ThemeHelper;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.SharedViewModelProviders;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.SwitchPreference;
import rikka.core.util.ResourceUtils;
import rikka.html.text.HtmlCompat;
import rikka.material.app.DayNightDelegate;
import rikka.material.app.LocaleDelegate;
import rikka.recyclerview.RecyclerViewHelper;

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
        viewModel.getPackages().observe(this, object -> {
            if (object.data != null)
                updateData(object.data);
        });
        if (viewModel.getPackages() != null
                && viewModel.getPackages().getValue() != null
                && viewModel.getPackages().getValue().data != null) {
            updateData(viewModel.getPackages().getValue().data);
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

        String tag = languagePreference.getValue();
        int index = Arrays.asList(languagePreference.getEntryValues()).indexOf(tag);

        List<String> localeName = new ArrayList<>();
        List<String> localeNameUser = new ArrayList<>();
        Locale userLocale = ShizukuManagerSettings.getLocale();
        for (int i = 1; i < languagePreference.getEntries().length; i++) {
            Locale locale = Locale.forLanguageTag(languagePreference.getEntries()[i].toString());
            localeName.add(!TextUtils.isEmpty(locale.getScript()) ? locale.getDisplayScript(locale) : locale.getDisplayName(locale));
            localeNameUser.add(!TextUtils.isEmpty(locale.getScript()) ? locale.getDisplayScript(userLocale) : locale.getDisplayName(userLocale));
        }
        for (int i = 1; i < languagePreference.getEntries().length; i++) {
            if (index != i) {
                languagePreference.getEntries()[i] = HtmlCompat.fromHtml(
                        String.format("%s - %s",
                                localeName.get(i - 1),
                                localeNameUser.get(i - 1)
                        ));
            } else {
                languagePreference.getEntries()[i] = localeNameUser.get(i - 1);
            }
        }

        if (TextUtils.isEmpty(tag) || "SYSTEM".equals(tag)) {
            languagePreference.setSummary(getString(R.string.follow_system));
        } else if (index != -1) {
            String name = localeNameUser.get(index - 1);
            languagePreference.setSummary(name);
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
        recyclerView.setPaddingRelative(recyclerView.getPaddingStart(), 0, recyclerView.getPaddingEnd(), padding);
        ViewGroup.LayoutParams _lp = recyclerView.getLayoutParams();
        if (_lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _lp;
            lp.leftMargin = lp.rightMargin = (int) recyclerView.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
        }
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

        noV2Preference.setSummary(requireContext().getResources().getQuantityString(R.plurals.start_legacy_service_summary, count, count));
    }
}
