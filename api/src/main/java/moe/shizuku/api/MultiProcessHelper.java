package moe.shizuku.api;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MultiProcessHelper {

    /**
     * For multi-process app only.
     * Call this method in every processes before using Shizuku.
     *
     * @param context context
     * @return true if successfully get binder from other process or called from provider's process or already have a binder alive
     */
    public static boolean initialize(Context context) {
        if (BinderReceiveProvider.isProviderProcess() || ShizukuService.pingBinder())
            return true;

        Bundle reply = context.getContentResolver().call(Uri.parse("content://" + context.getPackageName() + ".shizuku"),
                BinderReceiveProvider.METHOD_GET_BINDER, null, new Bundle());
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
