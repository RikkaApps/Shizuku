package moe.shizuku.manager.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

import moe.shizuku.manager.AuthorizationManager;
import moe.shizuku.manager.ShizukuManagerSettings;

/**
 * Created by Rikka on 2017/5/21.
 */

public class AuthProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        AuthorizationManager.init(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (getContext() == null) {
            return new TokenCursor(new UUID(0, 0));
        }

        UUID token = ShizukuManagerSettings.getToken(getContext());

        String packageName = getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        if (AuthorizationManager.granted(packageName)) {
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
