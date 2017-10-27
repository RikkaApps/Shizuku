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

import moe.shizuku.manager.Constants;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_PUT;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;

public class TransferProvider extends ContentProvider {

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
            throw new SecurityException("bad request");
        }

        Log.d(Constants.TAG, "" + Binder.getCallingUid());

        int id = extras.getInt(TRANSFER_PROVIDER_KEY_ID) + 1;

        if (TRANSFER_PROVIDER_METHOD_PUT.equals(method)) {
            return handlePut(id, arg, extras);
        } else if (TRANSFER_PROVIDER_METHOD_PUT.equals(method)) {
            return handleGet(id, arg);
        }

        throw new SecurityException("bad request");
    }

    private Bundle handlePut(int id, String arg, Bundle data) {
        // TODO: check permission

        if (TRANSFER_PROVIDER_TYPE_PARCELABLE.equals(arg)) {
            Parcelable p = data.getParcelable(TRANSFER_PROVIDER_KEY_DATA);
            sMap.append(id, p);

            Log.d(Constants.TAG, "put | id: " + id + " p: " + p);
        }
        return new Bundle();
    }

    private Bundle handleGet(int id, String type) {
        // TODO: check permission

        Bundle result = new Bundle();

        if (TRANSFER_PROVIDER_TYPE_PARCELABLE.equals(type)) {
            Parcelable p = (Parcelable) sMap.get(id);
            result.putParcelable(TRANSFER_PROVIDER_KEY_DATA, p);

            Log.d(Constants.TAG, "get | id: " + id + " p: " + p);

            sMap.remove(id);
        }
        return result;
    }
}
