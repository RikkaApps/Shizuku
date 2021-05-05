package moe.shizuku.manager.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import moe.shizuku.manager.R;
import rikka.core.util.ClipboardUtils;
import rikka.html.text.HtmlCompat;

/**
 * Created by fytho on 2017/12/15.
 */

public class CustomTabsHelper {

    public interface OnCreateIntentBuilderListener {
        void onCreateHelpIntentBuilder(Context context, CustomTabsIntent.Builder builder);
    }

    private static OnCreateIntentBuilderListener sOnCreateIntentBuilderListener;

    public static void setOnCreateIntentBuilderListener(OnCreateIntentBuilderListener onCreateIntentBuilderListener) {
        sOnCreateIntentBuilderListener = onCreateIntentBuilderListener;
    }

    private static TypedValue getTypedValue(Resources.Theme theme, int attrId) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attrId, typedValue, true);
        return typedValue;
    }

    private static @ColorInt
    int getColorIntFromAttr(Context context, @AttrRes int attrId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getTheme().getResources().getColor(getTypedValue(context.getTheme(), attrId).resourceId, context.getTheme());
        } else {
            return ContextCompat.getColor(context, getTypedValue(context.getTheme(), attrId).resourceId);
        }
    }

    public static CustomTabsIntent.Builder createBuilder(Context context) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getColorIntFromAttr(context, android.R.attr.colorPrimary));
        builder.setShowTitle(true);
        return builder;
    }

    public static boolean launchHelp(Context context, Uri uri) {
        CustomTabsIntent.Builder builder = createBuilder(context);

        if (sOnCreateIntentBuilderListener != null) {
            sOnCreateIntentBuilderListener.onCreateHelpIntentBuilder(context, builder);
        }

        Uri.Builder uriBuilder = uri.buildUpon();
        if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) > 0) {
            uriBuilder.appendQueryParameter("night", "1");
        }

        return launchUrl(context, builder.build(), uriBuilder.build());
    }

    public static boolean launchUrl(Context context, Uri uri) {
        return launchUrl(context, createBuilder(context).build(), uri);
    }

    private static boolean launchUrl(Context context, CustomTabsIntent customTabsIntent, Uri uri) {
        try {
            customTabsIntent.launchUrl(context, uri);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static void launchUrlOrCopy(Context context, String url) {
        Uri uri = Uri.parse(url);
        if (!launchHelp(context, uri)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            try {
                context.startActivity(intent);
            } catch (Throwable tr) {
                try {
                    ClipboardUtils.put(context, url);

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.dialog_cannot_open_browser_title)
                            .setMessage(HtmlCompat.fromHtml(context.getString(R.string.toast_copied_to_clipboard_with_text, url)))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}