package moe.shizuku.api;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ShizukuMultiProcessHelper {

    public static final String ACTION_BINDER_RECEIVED = "moe.shizuku.api.action.BINDER_RECEIVED";

    private static class BinderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            intent.setExtrasClassLoader(BinderContainer.class.getClassLoader());

            BinderContainer container = intent.getParcelableExtra(ShizukuApiConstants.EXTRA_BINDER);
            if (container != null && container.binder != null) {
                Log.i("ShizukuClient", "binder received from broadcast");
                ShizukuService.setBinder(container.binder);

                if (ShizukuClientHelper.getBinderReceivedListener() != null) {
                    ShizukuClientHelper.getBinderReceivedListener().onBinderReceived();
                }
            }
        }
    }

    private static BinderReceiver sReceiver;

    /**
     * For multi-process app only.
     * Call this method in every processes before using Shizuku.
     *
     * @param context context
     * @return true if successfully get binder from other process or called from provider's process or already have a binder alive
     *
     * @deprecated use {@link ShizukuMultiProcessHelper#initialize(Context, boolean)} instead
     */
    @Deprecated
    public static boolean initialize(Context context) {
        return initialize(context, ShizukuBinderReceiveProvider.isProviderProcess());
    }

    /**
     * For multi-process app only.
     * Call this method in every processes before using Shizuku.
     *
     * @param context context
     * @param isProviderProcess current process is the process of ShizukuBinderReceiveProvider
     * @return true if successfully get binder from other process or called from provider's process or already have a binder alive
     */
    public static boolean initialize(Context context, boolean isProviderProcess) {
        ShizukuBinderReceiveProvider.setIsProviderProcess(isProviderProcess);

        if (sReceiver == null && !isProviderProcess) {
            sReceiver = new BinderReceiver();
            context.getApplicationContext().registerReceiver(sReceiver, new IntentFilter(ACTION_BINDER_RECEIVED));
        }
        return isProviderProcess || ShizukuService.pingBinder() || requestBinder(context.getPackageName(), context.getContentResolver());
    }

    private static boolean requestBinder(String packageName, ContentResolver contentResolver) {
        Bundle reply;
        try {
            reply = contentResolver.call(Uri.parse("content://" + packageName + ".shizuku"),
                    ShizukuBinderReceiveProvider.METHOD_GET_BINDER, null, new Bundle());
        } catch (Throwable tr) {
            return false;
        }

        if (reply == null)
            return false;

        reply.setClassLoader(BinderContainer.class.getClassLoader());

        boolean res = false;
        BinderContainer container = reply.getParcelable(ShizukuApiConstants.EXTRA_BINDER);
        if (container != null && container.binder != null) {
            Log.i("ShizukuClient", "binder received from other process");
            ShizukuService.setBinder(container.binder);
            res = true;

            if (ShizukuClientHelper.getBinderReceivedListener() != null) {
                ShizukuClientHelper.getBinderReceivedListener().onBinderReceived();
            }
        }

        return res;
    }
}
