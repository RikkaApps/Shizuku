package moe.shizuku.privileged.api.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.UUID;

import moe.shizuku.privileged.api.Permissions;
import moe.shizuku.privileged.api.ServerLauncher;
import moe.shizuku.privileged.api.TokenCursor;

/**
 * Created by Rikka on 2017/5/21.
 */

public class AuthProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (getContext() == null) {
            return new TokenCursor(new UUID(0, 0));
        }

        UUID token = ServerLauncher.getToken(getContext());
        Permissions.init(getContext());

        String packageName = getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        if (Permissions.granted(packageName)) {
            return new TokenCursor(token);
        } else {
            return new TokenCursor(new UUID(0, 0));
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
