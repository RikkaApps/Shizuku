package moe.shizuku.manager.authorization;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationManager {

    private static final AuthorizationManagerImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 23) {
            IMPL = new AuthorizationManagerImplV23();
        } else {
            IMPL = new AuthorizationManagerImplV21();
        }
    }

    public static void init(Context context) {
        IMPL.init(context);
    }

    public static boolean granted(String packageName, int uid) {
        return IMPL.granted(packageName, uid);
    }

    public static void revoke(String packageName, int uid) {
        IMPL.revoke(packageName, uid);
    }

    public static void grant(String packageName, int uid) {
        IMPL.grant(packageName, uid);
    }

    public static List<PackageInfo> getPackages(int pmFlags) {
        return IMPL.getPackages(pmFlags);
    }

    public static List<PackageInfo> getGrantedPackages(int pmFlags) {
        List<PackageInfo> packages = new ArrayList<>();
        for (PackageInfo pi : IMPL.getPackages(pmFlags)) {
            if (granted(pi.packageName, pi.applicationInfo.uid)) {
                packages.add(pi);
            }
        }
        return packages;
    }
}
