package moe.shizuku.api;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BinderReceiveProvider extends ContentProvider {

    // For receive Binder from Shizuku
    public static final String METHOD_SEND_BINDER = "sendBinder";

    // For share Binder between processes
    public static final String METHOD_GET_BINDER = "getBinder";

    private static boolean sIsProviderProcess = false;

    @Override
    public boolean onCreate() {
        sIsProviderProcess = true;
        return true;
    }

    public static boolean isProviderProcess() {
        return sIsProviderProcess;
    }

    @Nullable
    @Override
    public final Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (extras == null)
            return null;

        Bundle reply = new Bundle();

        extras.setClassLoader(BinderContainer.class.getClassLoader());

        switch (method) {
            case METHOD_SEND_BINDER: {
                BinderContainer container = extras.getParcelable(ShizukuApiConstants.EXTRA_BINDER);
                if (container != null && container.binder != null) {
                    Log.i("ShizukuClient", "binder received");

                    ShizukuService.setBinder(container.binder);
                }
                // In order for user app report error, we always call listener even if the binder is null.
                // This provider is protected by INTERACT_ACROSS_USERS_FULL permission, other user apps can't use.
                if (ShizukuClientHelper.getBinderReceivedListener() != null) {
                    ShizukuClientHelper.getBinderReceivedListener().onBinderReceived();
                }
                break;
            }
            case METHOD_GET_BINDER: {
                // Other processes in the same app can read the provider without permission
                IBinder binder = ShizukuService.getBinder();
                if (binder == null) {
                    reply.putParcelable(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));
                }
                break;
            }
        }
        return reply;
    }

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
