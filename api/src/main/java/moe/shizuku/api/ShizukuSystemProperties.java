package moe.shizuku.api;

import android.os.RemoteException;

import androidx.annotation.NonNull;

import moe.shizuku.server.IShizukuService;

/**
 * @since added from version 9
 */
public class ShizukuSystemProperties {

    public static String get(String key) throws RemoteException {
        return ShizukuService.requireService().getSystemProperty(key, null);
    }

    public static String get(String key, String def) throws RemoteException {
        return ShizukuService.requireService().getSystemProperty(key, def);
    }

    public static int getInt(String key, int def) throws RemoteException {
        return Integer.decode(ShizukuService.requireService().getSystemProperty(key, Integer.toString(def)));
    }

    public static long getLong(String key, long def) throws RemoteException {
        return Long.decode(ShizukuService.requireService().getSystemProperty(key, Long.toString(def)));
    }

    public static boolean getBoolean(String key, boolean def) throws RemoteException {
        return Boolean.parseBoolean(ShizukuService.requireService().getSystemProperty(key, Boolean.toString(def)));
    }

    public static void set(String key, String val) throws RemoteException {
        ShizukuService.requireService().setSystemProperty(key, val);
    }
}
