package moe.shizuku.server.util;

import android.os.Process;
import android.util.Log;

/**
 * Created by Rikka on 2017/5/19.
 */

public class ServerLog {

    private static String TAG = "RServer " + Process.myPid();

    public static void v(String msg) {
        Log.v(TAG, msg);

        System.out.println(TAG + ": " + msg);
    }

    public static void v(String msg, Throwable tr) {
        Log.v(TAG, msg, tr);

        System.out.println(TAG + ": " + msg);
        tr.printStackTrace(System.out);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);

        System.out.println(TAG + ": " + msg);
    }

    public static void d(String msg, Throwable tr) {
        Log.d(TAG, msg, tr);

        System.out.println(TAG + ": " + msg);
        tr.printStackTrace(System.out);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);

        System.out.println(TAG + ": " + msg);
    }

    public static void i(String msg, Throwable tr) {
        Log.i(TAG, msg, tr);

        System.out.println(TAG + ": " + msg);
        tr.printStackTrace(System.out);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);

        System.out.println(TAG + ": " + msg);
    }

    public static void w(String msg, Throwable tr) {
        Log.w(TAG, msg, tr);

        System.out.println(TAG + ": " + msg);
        tr.printStackTrace(System.out);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);

        System.out.println(TAG + ": " + msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(TAG, msg, tr);

        System.err.println(TAG + ": " + msg);
        tr.printStackTrace(System.err);
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

        System.err.println(TAG + ": " + sb.toString().trim());
    }

    public static void wtf(String msg) {
        Log.wtf(TAG, msg);

        System.err.println(TAG + ": " + msg);
    }

    public static void wtf(String msg, Throwable tr) {
        Log.wtf(TAG, msg, tr);

        System.err.println(TAG + ": " + msg);
        tr.printStackTrace(System.err);
    }
}
