package moe.shizuku.server.util;

import android.util.Log;

/**
 * Created by Rikka on 2017/5/19.
 */

public class ServerLog {

    private static final String TAG = "RServer";

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void v(String msg, Throwable tr) {
        Log.v(TAG, msg, tr);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String msg, Throwable tr) {
        Log.d(TAG, msg, tr);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void i(String msg, Throwable tr) {
        Log.i(TAG, msg, tr);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        Log.w(TAG, msg, tr);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(TAG, msg, tr);
    }

    public static void eStack(String msg, Throwable tr) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append('\n');
        if (tr.getStackTrace() != null) {
            for (StackTraceElement ste : tr.getStackTrace()) {
                sb.append(ste.toString()).append('\n');
            }
        }
        Log.e(TAG, sb.toString().trim());
    }

    public static void wtf(String msg) {
        Log.wtf(TAG, msg);
    }

    public static void wtf(String msg, Throwable tr) {
        Log.wtf(TAG, msg, tr);
    }
}
