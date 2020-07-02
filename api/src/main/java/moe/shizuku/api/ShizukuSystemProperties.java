package moe.shizuku.api;

import android.os.RemoteException;

import androidx.annotation.NonNull;

import moe.shizuku.server.IShizukuService;

public class ShizukuSystemProperties {

    @NonNull
    private static IShizukuService requireService() {
        if (ShizukuService.sService == null) {
            throw new IllegalStateException("Binder haven't been received, check Shizuku and your code.");
        }
        return ShizukuService.sService;
    }

    public static String get(String key) throws RemoteException {
        return requireService().getSystemProperty(key, null);
    }

    public static String get(String key, String def) throws RemoteException {
        return requireService().getSystemProperty(key, def);
    }

    public static int getInt(String key, int def) throws RemoteException {
        return Integer.decode(requireService().getSystemProperty(key, Integer.toString(def)));
    }

    public static long getLong(String key, long def) throws RemoteException {
        return Long.decode(requireService().getSystemProperty(key, Long.toString(def)));
    }

    public static boolean getBoolean(String key, boolean def) throws RemoteException {
        return Boolean.parseBoolean(requireService().getSystemProperty(key, Boolean.toString(def)));
    }

    public static void set(String key, String val) throws RemoteException {
        requireService().setSystemProperty(key, val);
    }
}
