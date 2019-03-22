package moe.shizuku.manager.authorization;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.WorkerThread;

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

    public static boolean granted(String packageName) {
        return IMPL.granted(packageName);
    }

    public static void revoke(String packageName) {
        IMPL.revoke(packageName);
    }

    public static void grant(String packageName) {
        IMPL.grant(packageName);
    }

    public static List<String> getPackages() {
        return IMPL.getPackages();
    }

    public static List<String> getGrantedPackages() {
        List<String> packages = new ArrayList<>();
        for (String packageName : IMPL.getPackages()) {
            if (granted(packageName)) {
                packages.add(packageName);
            }
        }
        return packages;
    }
}
