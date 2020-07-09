package moe.shizuku.api;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * This provider receives binder from Shizuku server. When app process starts,
 * Shizuku server (it runs under adb/root) will send the binder to client apps with this provider.
 * </p>
 * <p>
 * Add the provider to your manifest like this:
 * </p>
 * <pre class="prettyprint">&lt;manifest&gt;
 *    ...
 *    &lt;application&gt;
 *        ...
 *        &lt;provider
 *            android:name="moe.shizuku.api.ShizukuProvider"
 *            android:authorities="${applicationId}.shizuku"
 *            android:exported="true"
 *            android:multiprocess="false"
 *            android:permission="android.permission.INTERACT_ACROSS_USERS_FULL"
 *        &lt;/provider&gt;
 *        ...
 *    &lt;/application&gt;
 * &lt;/manifest&gt;</pre>
 *
 * <p>
 * There are something needs you attention:
 * </p>
 * <ol>
 * <li><code>android:permission</code> shoule be a permission that granted to Shell (com.android.shell)
 * but not normal apps (e.g., android.permission.INTERACT_ACROSS_USERS_FULL), so that it can only
 * be used by the app itself and Shizuku server.</li>
 * <li><code>android:exported</code> must be <code>true</code> so that the provider can be accessed
 * from Shizuku server runs under adb.</li>
 * <li><code>android:multiprocess</code> must be <code>false</code>
 * since Shizuku server only gets uid when app starts.</li>
 * </ol>
 * <p>
 * If your app run in multiply processes, this provider also provides the functionality of sharing
 * the binder across processes. See {@link #enableMultiProcessSupport(Context, boolean)}.
 * </p>
 */
public class ShizukuProvider extends ContentProvider {

    private static final String TAG = "ShizukuProvider";

    // For receive Binder from Shizuku
    public static final String METHOD_SEND_BINDER = "sendBinder";

    // For share Binder between processes
    public static final String METHOD_GET_BINDER = "getBinder";

    public static final String ACTION_BINDER_RECEIVED = "moe.shizuku.api.action.BINDER_RECEIVED";

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    private static final List<OnBinderReceivedListener> LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Add a listener that will be called when binder is received.
     * <p>Note:</p>
     * <ul>
     * <li>The listener will be called in main thread.</li>
     * <li>The listener could be called multiply times. For example, user restarts Shizuku when app is running.</li>
     * </ul>
     * <p>
     *
     * @param listener OnBinderReceivedListener
     */
    public static void addBinderReceivedListener(@NonNull OnBinderReceivedListener listener) {
        addBinderReceivedListener(Objects.requireNonNull(listener), false);
    }

    /**
     * Same to {@link #addBinderReceivedListener(OnBinderReceivedListener)} but only call the listener
     * immediately if the binder is already received.
     *
     * @param listener OnBinderReceivedListener
     */
    public static void addBinderReceivedListenerSticky(@NonNull OnBinderReceivedListener listener) {
        addBinderReceivedListener(Objects.requireNonNull(listener), true);
    }

    private static void addBinderReceivedListener(@NonNull OnBinderReceivedListener listener, boolean sticky) {
        if (sticky && ShizukuService.pingBinder()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                listener.onBinderReceived();
            } else {
                mainHandler.post(listener::onBinderReceived);
            }
        }
        LISTENERS.add(listener);
    }

    /**
     * Remove the listener added by {@link #addBinderReceivedListener(OnBinderReceivedListener)}
     * or {@link #addBinderReceivedListenerSticky(OnBinderReceivedListener)}.
     *
     * @param listener OnBinderReceivedListener
     * @return If the listener is removed.
     */
    public static boolean removeBinderReceivedListener(@NonNull OnBinderReceivedListener listener) {
        return LISTENERS.remove(listener);
    }

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    static void postBinderReceivedListeners() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callBinderReceivedListeners();
        } else {
            mainHandler.post(ShizukuProvider::callBinderReceivedListeners);
        }
    }

    static void callBinderReceivedListeners() {
        for (OnBinderReceivedListener listener : LISTENERS) {
            listener.onBinderReceived();
        }
    }

    private static boolean enableMultiProcess = false;

    private static boolean isProviderProcess = false;

    public static void setIsProviderProcess(boolean isProviderProcess) {
        ShizukuProvider.isProviderProcess = isProviderProcess;
    }

    /**
     * Enables built-in multi-process support.
     * <p>
     * This method MUST be called as early as possible (e.g., static block in Application).
     */
    public static void enableMultiProcessSupport(boolean isProviderProcess) {
        Log.d(TAG, "enable built-in multi-process support (from " + (isProviderProcess ? "provider process" : "non-provider process") + ")");

        ShizukuProvider.isProviderProcess = isProviderProcess;
        ShizukuProvider.enableMultiProcess = true;
    }

    /**
     * Require binder for non-provider process, should have {@link #enableMultiProcessSupport(boolean)} called first.
     *
     * @param context Context
     */
    public static void requestBinderForNonProviderProcess(@NonNull Context context) {
        if (isProviderProcess) {
            return;
        }

        Log.d(TAG, "request binder in non-provider process");

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BinderContainer container = intent.getParcelableExtra(ShizukuApiConstants.EXTRA_BINDER);
                if (container != null && container.binder != null) {
                    Log.i(TAG, "binder received from broadcast");
                    ShizukuService.setBinder(container.binder);

                    postBinderReceivedListeners();
                }
            }
        }, new IntentFilter(ACTION_BINDER_RECEIVED));

        Bundle reply;
        try {
            reply = context.getContentResolver().call(Uri.parse("content://" + context.getPackageName() + ".shizuku"),
                    ShizukuProvider.METHOD_GET_BINDER, null, new Bundle());
        } catch (Throwable tr) {
            reply = null;
        }

        if (reply != null) {
            reply.setClassLoader(BinderContainer.class.getClassLoader());

            BinderContainer container = reply.getParcelable(ShizukuApiConstants.EXTRA_BINDER);
            if (container != null && container.binder != null) {
                Log.i(TAG, "binder received from other process");
                ShizukuService.setBinder(container.binder);

                postBinderReceivedListeners();
            }
        }
    }

    /**
     * Return if Shizuku app is installed.
     *
     * @param context Context
     * @return true if Shizuku app is installed
     */
    public static boolean isShizukuInstalled(@NonNull Context context) {
        try {
            return context.getPackageManager().getPermissionInfo(ShizukuApiConstants.PERMISSION, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

        if (info.multiprocess)
            throw new IllegalStateException("android:multiprocess must be false");

        if (!info.exported)
            throw new IllegalStateException("android:exported must be true");

        isProviderProcess = true;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (extras == null)
            return null;

        extras.setClassLoader(BinderContainer.class.getClassLoader());

        Bundle reply = new Bundle();
        switch (method) {
            case METHOD_SEND_BINDER: {
                handleSendBinder(extras);
                break;
            }
            case METHOD_GET_BINDER: {
                if (!handleGetBinder(reply)) {
                    return null;
                }
                break;
            }
        }
        return reply;
    }

    private void handleSendBinder(@NonNull Bundle extras) {
        if (ShizukuService.pingBinder()) {
            Log.d(TAG, "sendBinder called when already a living binder");
            return;
        }

        BinderContainer container = extras.getParcelable(ShizukuApiConstants.EXTRA_BINDER);
        if (container != null && container.binder != null) {
            Log.d(TAG, "binder received");

            ShizukuService.setBinder(container.binder);

            if (enableMultiProcess) {
                Log.d(TAG, "broadcast binder");

                //noinspection ConstantConditions
                Intent intent = new Intent(ACTION_BINDER_RECEIVED)
                        .putExtra(ShizukuApiConstants.EXTRA_BINDER, container)
                        .setPackage(getContext().getPackageName());
                getContext().sendBroadcast(intent);
            }

            postBinderReceivedListeners();
        }
    }

    private boolean handleGetBinder(@NonNull Bundle reply) {
        // Other processes in the same app can read the provider without permission
        IBinder binder = ShizukuService.getBinder();
        if (binder == null || !binder.pingBinder())
            return false;

        reply.putParcelable(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));
        return true;
    }

    // no other provider methods
    @Nullable
    @Override
    public final Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public final String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public final Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public final int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public final int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
