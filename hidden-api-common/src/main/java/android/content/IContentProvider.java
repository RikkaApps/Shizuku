package android.content;

import android.annotation.Nullable;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ICancellationSignal;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import java.io.FileNotFoundException;

public interface IContentProvider {

    Cursor query(String callingPkg, Uri url, String[] projection, String selection,
                 String[] selectionArgs, String sortOrder, ICancellationSignal cancellationSignal)
            throws RemoteException;

    @RequiresApi(26)
    Cursor query(String callingPkg, Uri url, @Nullable String[] projection,
                 @Nullable Bundle queryArgs, @Nullable ICancellationSignal cancellationSignal)
            throws RemoteException;

    AssetFileDescriptor openAssetFile(
            String callingPkg, Uri url, String mode, ICancellationSignal signal)
            throws RemoteException, FileNotFoundException;
}
