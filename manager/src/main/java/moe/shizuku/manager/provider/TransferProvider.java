package moe.shizuku.manager.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.Random;

import moe.shizuku.manager.authorization.AuthorizationManager;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_GET;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_PUT;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;

public class TransferProvider extends ContentProvider {

    private static final String TAG = "TransferProvider";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("unsupported");
    }

    private static SparseArray<Object> sMap = new SparseArray<>();

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (arg == null || extras == null) {
            return null;
        }

        if (!checkPermission()) {
            return null;
        }

        if (TRANSFER_PROVIDER_METHOD_PUT.equals(method)) {
            return handlePut(arg, extras);
        } else if (TRANSFER_PROVIDER_METHOD_GET.equals(method)) {
            return handleGet(arg, extras);
        }

        return null;
    }

    private boolean checkPermission() {
        String packageName = getCallingPackage();

        if (packageName != null
                && !AuthorizationManager.granted(getContext(), packageName)) {
            Log.w(TAG, "Package " + packageName + " try to call provider without permission.");
            return false;
        }
        return true;
    }

    private Bundle handlePut(String arg, Bundle data) {
        int uid = Binder.getCallingUid();
        int id = generateId();

        if (TRANSFER_PROVIDER_TYPE_PARCELABLE.equals(arg)) {
            Parcelable p = data.getParcelable(TRANSFER_PROVIDER_KEY_DATA);
            sMap.append(id, p);

            Log.d(TAG, "put | key: " + id + " parcelable: " + p);
        }
        Bundle result = new Bundle();
        result.putInt(TRANSFER_PROVIDER_KEY_ID, id);
        return result;
    }

    private Bundle handleGet(String type, Bundle data) {
        Bundle result = new Bundle();

        int id = data.getInt(TRANSFER_PROVIDER_KEY_ID);

        if (TRANSFER_PROVIDER_TYPE_PARCELABLE.equals(type)) {
            Parcelable p = (Parcelable) sMap.get(id);
            result.putParcelable(TRANSFER_PROVIDER_KEY_DATA, p);

            Log.d(TAG, "get | key: " + id + " parcelable: " + p);

            sMap.remove(id);
        }
        return result;
    }

    private static int generateId() {
        int id = new Random().nextInt(Integer.MAX_VALUE);
        if (sMap.indexOfKey(id) >= 0) {
            return generateId();
        }
        return id;
    }
}
