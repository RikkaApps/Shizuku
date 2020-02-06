package moe.shizuku.manager.app;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.shizuku.fontprovider.FontProviderClient;
import rikka.core.util.ResourceUtils;
import rikka.material.app.MaterialActivity;

public abstract class BaseActivity extends MaterialActivity {

    private static boolean sFontInitialized = false;

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

        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public String computeUserThemeKey() {
        return ThemeHelper.getTheme(this);
    }

    @Override
    public void onApplyUserThemeResource(@NonNull Resources.Theme theme, boolean isDecorView) {
        theme.applyStyle(ThemeHelper.getThemeStyleRes(this), true);
    }

    @Override
    public boolean shouldApplyTranslucentSystemBars() {
        return Build.VERSION.SDK_INT >= 23;
    }

    @Override
    public void onApplyTranslucentSystemBars() {
        final Window window = getWindow();
        final View root = findViewById(android.R.id.content);
        if (window == null || root == null) {
            return;
        }

        int paddingTop = root.getPaddingTop();
        int paddingLeft = root.getPaddingLeft();
        int paddingRight = root.getPaddingRight();
        int paddingBottom = root.getPaddingBottom();

        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            View list = findViewById(android.R.id.list);
            if (list != null) {
                root.setPadding(paddingLeft + insets.getSystemWindowInsetLeft(),
                        paddingTop + insets.getSystemWindowInsetTop(), paddingRight + insets.getSystemWindowInsetRight(), paddingBottom);

                list.setPadding(list.getPaddingLeft(), list.getPaddingTop(), list.getPaddingRight(), insets.getSystemWindowInsetBottom());
            } else {
                root.setPadding(paddingLeft + insets.getSystemWindowInsetLeft(),
                        paddingTop + insets.getSystemWindowInsetTop(),
                        paddingRight + insets.getSystemWindowInsetRight(),
                        paddingBottom + +insets.getStableInsetBottom());
            }

            if (insets.getSystemWindowInsetBottom() >= Resources.getSystem().getDisplayMetrics().density * 40) {
                int alpha = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 0xe0000000 : 0x60000000;
                window.setNavigationBarColor(ResourceUtils.resolveColor(getTheme(), android.R.attr.navigationBarColor) & 0x00ffffff | alpha);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    window.setNavigationBarDividerColor(ResourceUtils.resolveColor(getTheme(), android.R.attr.navigationBarDividerColor));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.setNavigationBarContrastEnforced(true);
                    }
                }
            }

            return insets.consumeSystemWindowInsets();
        });

        //window.setStatusBarColor(ResourceUtils.resolveColor(getTheme(), R.attr.colorPrimaryVariant));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.setNavigationBarDividerColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(true);
            }
        }
    }
}
