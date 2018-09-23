package moe.shizuku.manager.authorization;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.WorkerThread;

/**
 * Created by Rikka on 2017/5/17.
 */

@WorkerThread
public class AuthorizationManager {

    private static AuthorizationManagerImpl sImpl;

    static {
        if (Build.VERSION.SDK_INT >= 23) {
            sImpl = new AuthorizationManagerImplV23();
        } else {
            sImpl = new AuthorizationManagerImplV21();
        }
    }

    public static boolean granted(Context context, String packageName) {
        return sImpl.granted(context, packageName);
    }

    public static void revoke(Context context, String packageName) {
        sImpl.revoke(context, packageName);
    }

    public static void grant(Context context, String packageName) {
        sImpl.grant(context, packageName);
    }

    public static List<String> getPackages(Context context) {
        return sImpl.getPackages(context);
    }

    public static List<String> getGrantedPackages(Context context) {
        List<String> packages = new ArrayList<>();
        for (String packageName : sImpl.getPackages(context)) {
            if (granted(context, packageName)) {
                packages.add(packageName);
            }
        }
        return packages;
    }
}
