package moe.shizuku.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public class ShizukuClientHelper {

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    private static OnBinderReceivedListener sBinderReceivedListener;

    public static OnBinderReceivedListener getBinderReceivedListener() {
        return sBinderReceivedListener;
    }

    public static void setBinderReceivedListener(@Nullable OnBinderReceivedListener binderReceivedListener) {
        sBinderReceivedListener = binderReceivedListener;
    }

    public static boolean isManagerV3Installed(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 183;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isManagerV2Installed(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 106;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getManagerVersionCode(@NonNull Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static int getLatestVersion() {
        return ShizukuApiConstants.SERVER_VERSION;
    }
}
