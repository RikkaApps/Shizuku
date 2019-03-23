package moe.shizuku.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.support.app.DayNightDelegate;
import moe.shizuku.support.app.LocaleDelegate;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;
import moe.shizuku.support.utils.HtmlUtils;

public class SettingsFragment extends PreferenceFragment {

    public static final String KEY_LANGUAGE = ShizukuManagerSettings.LANGUAGE;
    public static final String KEY_NIGHT_MODE = ShizukuManagerSettings.NIGHT_MODE;

    private ListPreference languagePreference;
    private Preference nightModePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        getPreferenceManager().setSharedPreferencesName(ShizukuManagerSettings.NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        setPreferencesFromResource(R.xml.settings, null);

        languagePreference = (ListPreference) findPreference(KEY_LANGUAGE);
        nightModePreference = findPreference(KEY_NIGHT_MODE);

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
}
