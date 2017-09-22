package moe.shizuku.io;

import android.graphics.Bitmap;
import android.os.BadParcelableException;
import android.os.NetworkOnMainThreadException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ServiceSpecificException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Rikka on 2017/5/18.
 */

public class ParcelOutputStream extends DataOutputStream {

    private static final int EX_SECURITY = -1;
    private static final int EX_BAD_PARCELABLE = -2;
    private static final int EX_ILLEGAL_ARGUMENT = -3;
    private static final int EX_NULL_POINTER = -4;
    private static final int EX_ILLEGAL_STATE = -5;
    private static final int EX_NETWORK_MAIN_THREAD = -6;
    private static final int EX_UNSUPPORTED_OPERATION = -7;
    private static final int EX_SERVICE_SPECIFIC = -8;
    private static final int EX_UNKNOWN = -128;

    /**
     * Creates a new data output stream to write data to the specified
     * underlying output stream. The counter <code>written</code> is
     * set to zero.
     *
     * @param out the underlying output stream, to be saved for later
     *            use.
     * @see FilterOutputStream#out
     */
    public ParcelOutputStream(OutputStream out) {
        super(out);
    }

    public final void writeString(String str) throws IOException {
        if (str == null) {
            writeInt(-1);
        } else {
            writeInt(0);
            writeUTF(str);
        }
    }

    public final void writeNoException() throws IOException {
        writeInt(0);
    }

    public final void writeException(Throwable e) throws IOException {
        int code = EX_UNKNOWN;
        if (e instanceof SecurityException) {
            code = EX_SECURITY;
        } else if (e instanceof BadParcelableException) {
            code = EX_BAD_PARCELABLE;
        } else if (e instanceof IllegalArgumentException) {
            code = EX_ILLEGAL_ARGUMENT;
        } else if (e instanceof NullPointerException) {
            code = EX_NULL_POINTER;
        } else if (e instanceof IllegalStateException) {
            code = EX_ILLEGAL_STATE;
        } else if (e instanceof NetworkOnMainThreadException) {
            code = EX_NETWORK_MAIN_THREAD;
        } else if (e instanceof UnsupportedOperationException) {
            code = EX_UNSUPPORTED_OPERATION;
        } else if (e instanceof ServiceSpecificException) {
            code = EX_SERVICE_SPECIFIC;
        }
        writeInt(code);
        writeString(e.getMessage());
    }

    public final void writeBytes(byte[] bytes) throws IOException {
        if (bytes == null) {
            writeInt(-1);
            return;
        }

        int size = bytes.length;
        writeInt(size);
        write(bytes);
    }

    public final void writeParcelable(Parcelable parcelable) throws IOException {
        if (parcelable == null) {
            writeInt(-1);
            return;
        }

        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();

        writeBytes(bytes);
    }

    public final void writeParcelableList(List<? extends Parcelable> list) throws IOException {
        if (list == null) {
            writeInt(-1);
            return;
        }

        Parcel parcel = Parcel.obtain();
        parcel.writeTypedList(list);
        byte[] bytes = parcel.marshall();
        parcel.recycle();

        writeBytes(bytes);
    }

    public final void writeBitmap(Bitmap bitmap) throws IOException {
        if (bitmap == null) {
            writeInt(-1);
            return;
        }

        // TODO do not do that?
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bytes = bos.toByteArray();

        writeBytes(bytes);
    }
}
