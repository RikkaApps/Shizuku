package moe.shizuku.manager.utils;

import android.util.Log;

import java.util.Locale;

public class Logger {

    public static final Logger LOGGER = new Logger("ShizukuManager");

    private String TAG;

    public Logger(String TAG) {
        this.TAG = TAG;
    }

    public boolean isLoggable(String tag, int level) {
        return true;
    }

    public void v(String msg) {
        if (isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, msg);
        }
    }

    public void v(String fmt, Object... args) {
        if (isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, String.format(Locale.ENGLISH, fmt, args));
        }
    }

    public void v(String msg, Throwable tr) {
        if (isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, msg, tr);
        }
    }

    public void d(String msg) {
        if (isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, msg);
        }
    }

    public void d(String fmt, Object... args) {
        if (isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format(Locale.ENGLISH, fmt, args));
        }
    }

    public void d(String msg, Throwable tr) {
        if (isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, msg, tr);
        }
    }

    public void i(String msg) {
        if (isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, msg);
        }
    }

    public void i(String fmt, Object... args) {
        if (isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, String.format(Locale.ENGLISH, fmt, args));
        }
    }

    public void i(String msg, Throwable tr) {
        if (isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, msg, tr);
        }
    }

    public void w(String msg) {
        if (isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, msg);
        }
    }

    public void w(String fmt, Object... args) {
        if (isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, String.format(Locale.ENGLISH, fmt, args));
        }
    }

    public void w(Throwable tr, String fmt, Object... args) {
        if (isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, String.format(Locale.ENGLISH, fmt, args), tr);
        }
    }

    public void w(String msg, Throwable tr) {
        if (isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, msg, tr);
        }
    }

    public void e(String msg) {
        if (isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, msg);
        }
    }

    public void e(String fmt, Object... args) {
        if (isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, String.format(Locale.ENGLISH, fmt, args));
        }
    }

    public void e(String msg, Throwable tr) {
        if (isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, msg, tr);
        }
    }

    public void e(Throwable tr, String fmt, Object... args) {
        if (isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, String.format(Locale.ENGLISH, fmt, args), tr);
        }
    }
}
