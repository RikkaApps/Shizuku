package moe.shizuku.server.io;

import android.content.IContentProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.system.Os;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.server.api.Compat;
import moe.shizuku.server.util.ServerLog;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_PUT;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_NAME;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;

/**
 * Created by rikka on 2017/10/27.
 */

public class ServerParcelOutputStream extends ParcelOutputStream {

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see FilterOutputStream#out
     */
    public ServerParcelOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Send ParcelFileDescriptor to client via ContentProvider.
     *
     * @param userId client user id
     * @param pfd ParcelFileDescriptor
     */
    public final void writeParcelFileDescriptor(int userId, ParcelFileDescriptor pfd) throws IOException {
        int key = -1;

        try {
            IBinder token = new Binder();
            IContentProvider provider = Compat.getContentProvider(TRANSFER_PROVIDER_NAME, userId, token);
            if (provider != null) {
                Bundle data = new Bundle();
                data.putParcelable(TRANSFER_PROVIDER_KEY_DATA, pfd);

                ServerLog.i("path: " + Os.readlink("/proc/self/fd/" + pfd.getFd()));
                ServerLog.i("call send | fd: " + pfd.getFd() + " data: " + data.toString());

                Bundle result = provider.call(null, TRANSFER_PROVIDER_METHOD_PUT, TRANSFER_PROVIDER_TYPE_PARCELABLE, data);

                key = result.getInt(TRANSFER_PROVIDER_KEY_ID);
            }
        } catch (Exception e) {
            ServerLog.e("failed send ParcelFileDescriptor to manager app", e);
        }

        writeInt(key);
    }
}
