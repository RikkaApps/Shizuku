package moe.shizuku.server.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BadParcelableException;
import android.os.NetworkOnMainThreadException;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by Rikka on 2017/5/18.
 */

public class ParcelInputStream extends DataInputStream {

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
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ParcelInputStream(@NonNull InputStream in) {
        super(in);
    }

    public final String readString() throws IOException {
        if (readInt() == -1) {
            return null;
        } else {
            return readUTF();
        }
    }

    public final void readException() throws IOException {
        int code = readInt();
        if (code != 0) {
            String msg = readString();
            readException(code, msg);
        }
    }

    private final void readException(int code, String msg) throws IOException {
        switch (code) {
            case EX_SECURITY:
                throw new SecurityException(msg);
            case EX_BAD_PARCELABLE:
                throw new BadParcelableException(msg);
            case EX_ILLEGAL_ARGUMENT:
                throw new IllegalArgumentException(msg);
            case EX_NULL_POINTER:
                throw new NullPointerException(msg);
            case EX_ILLEGAL_STATE:
                throw new IllegalStateException(msg);
            case EX_NETWORK_MAIN_THREAD:
                throw new NetworkOnMainThreadException();
            case EX_UNSUPPORTED_OPERATION:
                throw new UnsupportedOperationException(msg);
        }
        throw new PrivilegedServerException(msg);
    }

    public final byte[] readBytes() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }

        byte[] bytes = new byte[size];
        int length;
        int offset = 0;
        int remain = bytes.length;
        while (remain > 0 && (length = read(bytes, offset, remain)) != -1) {
            if (length > 0) {
                offset += length;
                remain -= length;
            }
        }
        return bytes;
    }

    public final <T> T readParcelable(Parcelable.Creator<T> creator) throws IOException {
        byte[] bytes = readBytes();

        if (bytes == null) {
            return null;
        }

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();

        return result;
    }

    public final <T> List<T> readParcelableList(Parcelable.Creator<T> creator) throws IOException {
        byte[] bytes = readBytes();

        if (bytes == null) {
            return null;
        }

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        List<T> result = parcel.createTypedArrayList(creator);
        parcel.recycle();

        return result;
    }

    public final Bitmap readBitmap() throws IOException {
        byte[] bytes = readBytes();

        if (bytes == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }
}
