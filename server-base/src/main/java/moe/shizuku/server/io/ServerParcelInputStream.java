package moe.shizuku.server.io;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.io.InputStream;

import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.server.api.Compat;
import moe.shizuku.server.util.ServerLog;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_GET;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_NAME;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_BINDER;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;

/**
 * Created by rikka on 2017/10/27.
 */

public class ServerParcelInputStream extends ParcelInputStream {

    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ServerParcelInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read IBinder from client via ContentProvider.
     * <p>
     * Keep sync with ParcelOutputStream<br>
     * int: key<br>
     * int: client userId<br>
     *
     * @return IBinder
     * @throws IOException connection problem
     */
    @Override
    public IBinder readBinder() throws IOException {
        int key = readInt();
        if (key == -1) {
            return null;
        }

        int userId = readInt();

        Bundle bundle = new Bundle();
        bundle.putInt(TRANSFER_PROVIDER_KEY_ID, key);

        try {
            return Compat.getContentProvider(TRANSFER_PROVIDER_NAME, userId, new Binder())
                    .call(null, TRANSFER_PROVIDER_METHOD_GET, TRANSFER_PROVIDER_TYPE_BINDER, bundle)
                    .getBinder(TRANSFER_PROVIDER_KEY_DATA);
        } catch (Exception e) {
            ServerLog.e("failed read Binder from manager app", e);
            return null;
        }
    }

    /**
     * Read ParcelFileDescriptor from client via ContentProvider.
     * <p>
     * Keep sync with ParcelOutputStream<br>
     * int: key<br>
     * int: client userId<br>
     *
     * @return ParcelFileDescriptor
     * @throws IOException
     */
    @Override
    public final ParcelFileDescriptor readParcelFileDescriptor() throws IOException {
        int key = readInt();
        if (key == -1) {
            return null;
        }

        int userId = readInt();

        Bundle bundle = new Bundle();
        bundle.putInt(TRANSFER_PROVIDER_KEY_ID, key);

        try {
            return Compat.getContentProvider(TRANSFER_PROVIDER_NAME, userId, new Binder())
                    .call(null, TRANSFER_PROVIDER_METHOD_GET, TRANSFER_PROVIDER_TYPE_PARCELABLE, bundle)
                    .getParcelable(TRANSFER_PROVIDER_KEY_DATA);
        } catch (Exception e) {
            ServerLog.e("failed read ParcelFileDescriptor from manager app", e);
            return null;
        }
    }
}
