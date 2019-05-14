package moe.shizuku.manager.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

import moe.shizuku.fontprovider.FontProviderClient;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.support.app.DayNightDelegate;
import moe.shizuku.support.app.LocaleDelegate;
import moe.shizuku.support.utils.ResourceUtils;

public abstract class BaseActivity extends FragmentActivity {

    private static boolean sFontInitialized = false;

    private LocaleDelegate mLocaleDelegate;
    private DayNightDelegate mDayNightDelegate;
    private String mTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontInitialized && Build.VERSION.SDK_INT < 28) {
            FontProviderClient client = FontProviderClient.create(this);
            if (client != null) {
                client.replace("Noto Sans CJK",
                        "sans-serif", "sans-serif-medium");
            }

            sFontInitialized = true;
        }

        getLocaleDelegate().onCreate(this);
        mTheme = ThemeHelper.getTheme(this);

        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().setElevation(0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getLocaleDelegate().isLocaleChanged()
                || getDayNightDelegate().getNightMode() != DayNightDelegate.getDefaultNightMode()
                || !Objects.equals(mTheme, ThemeHelper.getTheme(this))) {
            recreate();
        }
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        // apply real style and our custom style
        if (getParent() == null) {
            theme.applyStyle(resid, true);
        } else {
            try {
                theme.setTo(getParent().getTheme());
            } catch (Exception e) {
                // Empty
            }
            theme.applyStyle(resid, false);
        }
        theme.applyStyle(ThemeHelper.getThemeStyleRes(this), true);
        // only pass theme style to super, so styled theme will not be overwritten
        super.onApplyThemeResource(theme, R.style.ThemeOverlay, first);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDayNightDelegate().cleanup();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getDayNightDelegate().onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDayNightDelegate().cleanup();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration configuration = newBase.getResources().getConfiguration();
        getLocaleDelegate().updateConfiguration(configuration);
        getDayNightDelegate(newBase).updateConfiguration(configuration);

        super.attachBaseContext(newBase.createConfigurationContext(configuration));
    }

    public LocaleDelegate getLocaleDelegate() {
        if (mLocaleDelegate == null) {
            mLocaleDelegate = new LocaleDelegate();
        }
        return mLocaleDelegate;
    }

    public DayNightDelegate getDayNightDelegate() {
        if (mDayNightDelegate == null) {
            mDayNightDelegate = new DayNightDelegate(this, DayNightDelegate.getDefaultNightMode());
        }
        return mDayNightDelegate;
    }

    private DayNightDelegate getDayNightDelegate(Context context) {
        if (mDayNightDelegate == null) {
            mDayNightDelegate = new DayNightDelegate(context.getApplicationContext(), DayNightDelegate.getDefaultNightMode());
        }
        return mDayNightDelegate;
    }
}
