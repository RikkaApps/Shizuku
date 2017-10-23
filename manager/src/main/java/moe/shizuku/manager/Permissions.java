package moe.shizuku.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Rikka on 2017/5/17.
 */

public class Permissions {

    private static final String NAME = "settings";
    private static final String KEY = "granted_packages";

    private SharedPreferences sharedPreferences;
    private Set<Pair<String, Long>> packages;

    private static Permissions sPermissions;

    public static void init(Context context) {
        sPermissions = new Permissions(context);
    }

    private Permissions(Context context) {
        this.sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        this.packages = new HashSet<>();
        this.packages = fromPreference(context, sharedPreferences.getStringSet(KEY, new HashSet<String>()));
    }

    public static boolean granted(String packageName) {
        if (packageName == null) {
            return false;
        }
        for (Pair<String, Long> p : sPermissions.packages) {
            if (p.first.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static int grantedCount() {
        return sPermissions.packages.size();
    }

    public static void set(String packageName, long firstInstallTime, boolean grant) {
        if (grant) {
            grant(packageName, firstInstallTime);
        } else {
            revoke(packageName);
        }
    }

    public static void toggle(String packageName, long firstInstallTime) {
        if (!granted(packageName)) {
            grant(packageName, firstInstallTime);
        } else {
            revoke(packageName);
        }
    }

    private static void remove(String packageName) {
        for (Pair<String, Long> p : new HashSet<>(sPermissions.packages)) {
            if (p.first.equals(packageName)) {
                sPermissions.packages.remove(p);
            }
        }
    }

    public static void grant(String packageName, long firstInstallTime) {
        remove(packageName);

        sPermissions.packages.add(new Pair<>(packageName, firstInstallTime));
        sPermissions.sharedPreferences.edit()
                .putStringSet(KEY, toPreference(sPermissions.packages))
                .apply();
    }

    public static void revoke(String packageName) {
        remove(packageName);

        sPermissions.sharedPreferences.edit()
                .putStringSet(KEY, toPreference(sPermissions.packages))
                .apply();
    }

    private static Set<Pair<String, Long>> fromPreference(Context context, Set<String> from) {
        PackageManager pm = context.getPackageManager();

        Set<Pair<String, Long>> to = new HashSet<>();
        for (String p: from) {
            String[] temp = p.split("\\|");
            if (temp.length == 2) {
                String packageName = temp[0];
                long firstInstallTime = Long.parseLong(temp[1]);

                try {
                    if (pm.getPackageInfo(packageName, 0).firstInstallTime == firstInstallTime) {
                        to.add(new Pair<>(packageName, firstInstallTime));
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
        }
        return to;
    }

    private static Set<String> toPreference(Set<Pair<String, Long>> from) {
        Set<String> to = new HashSet<>();
        for (Pair<String, Long> p: from) {
            to.add(p.first + "|" + p.second);
        }
        return to;
    }

    public static List<String> getGranted() {
        List<String> list = new ArrayList<>();
        for (Pair<String, Long> p: sPermissions.packages) {
            list.add(p.first);
        }
        return list;
    }
}
