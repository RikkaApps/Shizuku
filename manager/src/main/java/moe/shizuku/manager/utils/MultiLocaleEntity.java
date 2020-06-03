package moe.shizuku.manager.utils;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Locale;

import moe.shizuku.manager.ShizukuSettings;

public class MultiLocaleEntity extends LinkedHashMap<String, String> {

    public abstract static class LocaleProvider {
        public abstract Locale get();
    }

    public static final LocaleProvider DEFAULT_LOCAL_PROVIDER = new LocaleProvider() {
        @Override
        public Locale get() {
            return ShizukuSettings.getLocale();
        }
    };

    private static LocaleProvider sLocaleProvider = DEFAULT_LOCAL_PROVIDER;

    public static void setLocaleProvider(@NonNull LocaleProvider localeProvider) {
        sLocaleProvider = localeProvider;
    }

    public String get() {
        return get(sLocaleProvider.get());
    }

    public String get(@NonNull Locale locale) {
        if (size() > 0) {
            String language = locale.getLanguage();
            String region = locale.getCountry();

            // fully match
            locale = new Locale(language, region);
            for (String l : keySet()) {
                if (locale.toString().equals(l.replace('-', '_'))) {
                    return get(l);
                }
            }

            // match language only keys
            locale = new Locale(language);
            for (String l : keySet()) {
                if (locale.toString().equals(l)) {
                    return get(l);
                }
            }

            // match a language_region with only language
            for (String l : keySet()) {
                if (l.startsWith(locale.toString())) {
                    return get(l);
                }
            }

            if (containsKey("en")) {
                return get("en");
            }

            if (containsKey("default")) {
                return get("default");
            }

            for (String key : keySet()) {
                if (!"overwrite_default".equals(key))
                    return get(key);
            }
        }
        return null;
    }
}
