package moe.shizuku.server.io;

import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.server.api.Compat;
import moe.shizuku.server.util.ServerLog;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_GET;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_NAME;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_URI;

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
     * Read ParcelFileDescriptor from client via ContentProvider.
     * <p>
     * Keep sync with ParcelOutputStream<br>
     * int: client userId<br>
     * int: key<br>
     *
     * @return ParcelFileDescriptor
     * @throws IOException
     */
    @Override
    public final ParcelFileDescriptor readParcelFileDescriptor() throws IOException {
        int userId = readInt();
        int key = readInt();

        if (key == -1) {
            return null;
        }

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
