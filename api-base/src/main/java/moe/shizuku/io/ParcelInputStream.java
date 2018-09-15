package moe.shizuku.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.NetworkOnMainThreadException;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import moe.shizuku.api.ShizukuClient;
import moe.shizuku.lang.ShizukuRemoteException;

import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_DATA;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_KEY_ID;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_METHOD_GET;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_BINDER;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_TYPE_PARCELABLE;
import static moe.shizuku.ShizukuConstants.TRANSFER_PROVIDER_URI;

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
    public ParcelInputStream(InputStream in) {
        super(in);
    }

    public final boolean[] readBooleanArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        boolean[] booleans = new boolean[size];
        for (int i = 0; i < size; i++) {
            booleans[i] = readBoolean();
        }
        return booleans;
    }

    public final byte[] readByteArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    public final short[] readShortArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        short[] shorts = new short[size];
        for (int i = 0; i < size; i++) {
            shorts[i] = readShort();
        }
        return shorts;
    }

    public final char[] readCharArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        char[] chars = new char[size];
        for (int i = 0; i < size; i++) {
            chars[i] = readChar();
        }
        return chars;
    }

    public final int[] readIntArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        int[] ints = new int[size];
        for (int i = 0; i < size; i++) {
            ints[i] = readInt();
        }
        return ints;
    }

    public final long[] readLongArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        long[] longs = new long[size];
        for (int i = 0; i < size; i++) {
            longs[i] = readLong();
        }
        return longs;
    }

    public final float[] readFloatArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            floats[i] = readFloat();
        }
        return floats;
    }

    public final double[] readDoubleArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        double[] doubles = new double[size];
        for (int i = 0; i < size; i++) {
            doubles[i] = readDouble();
        }
        return doubles;
    }

    public final CharSequence readCharSequence() throws IOException {
        if (readInt() == -1) {
            return null;
        } else {
            return readString();
        }
    }

    public final String readString() throws IOException {
        if (readInt() == -1) {
            return null;
        } else {
            return readUTF();
        }
    }

    public final String[] readStringArray() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = readString();
        }
        return strings;
    }

    public List<String> readStringList() throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }
        List<String> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            strings.add(readString());
        }
        return strings;
    }

    public final void readException() throws IOException, ShizukuRemoteException {
        int code = readInt();
        if (code != 0) {
            String msg = readString();
            readException(code, msg);
        }
    }

    private final void readException(int code, String msg) throws IOException, ShizukuRemoteException {
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
            /*case EX_SERVICE_SPECIFIC:
                throw new ServiceSpecificException(readInt(), msg);*/
        }
        throw new ShizukuRemoteException(msg);
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

    public final <T> T readParcelable(Class<?> cls) throws IOException {
        byte[] bytes = readBytes();

        if (bytes == null) {
            return null;
        }

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        try {
            Constructor constructor = cls.getConstructor(Parcel.class);
            //noinspection unchecked
            T result = (T) constructor.newInstance(parcel);
            parcel.recycle();
            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }
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

    public <T> T[] readParcelableArray(Parcelable.Creator<T> creator) throws IOException {
        int size = readInt();
        if (size == -1) {
            return null;
        }

        T[] parcelables = creator.newArray(size);
        for (int i = 0; i < size; i++) {
            parcelables[i] = readParcelable(creator);
        }
        return parcelables;
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

    /**
     * Read IBinder from server via ContentProvider.
     * <p>
     * int: key
     *
     * @return IBinder
     * @throws IOException connection problem
     */
    public IBinder readBinder() throws IOException {
        int key = readInt();
        if (key == -1) {
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(TRANSFER_PROVIDER_KEY_ID, key);

        return ShizukuClient.getContext().getContentResolver()
                .call(TRANSFER_PROVIDER_URI, TRANSFER_PROVIDER_METHOD_GET, TRANSFER_PROVIDER_TYPE_BINDER, bundle)
                .getBinder(TRANSFER_PROVIDER_KEY_DATA);
    }

    public final List<IBinder> readBinderList() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        readByte();
        return null;
    }

    public final <T extends IInterface> List<T> readInterfaceList() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        readByte();
        return null;
    }

    /*public final IBinder[] readBinderArray() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        readByte();
        return null;
    }

    public final IInterface[] readInterfaceArray() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        readByte();
        return null;
    }*/

    /**
     * Read ParcelFileDescriptor from server via ContentProvider.
     * <p>
     * int: key
     *
     * @return ParcelFileDescriptor
     * @throws IOException connection problem
     */
    public ParcelFileDescriptor readParcelFileDescriptor() throws IOException {
        int key = readInt();
        if (key == -1) {
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(TRANSFER_PROVIDER_KEY_ID, key);

        return ShizukuClient.getContext().getContentResolver()
                .call(TRANSFER_PROVIDER_URI, TRANSFER_PROVIDER_METHOD_GET, TRANSFER_PROVIDER_TYPE_PARCELABLE, bundle)
                .getParcelable(TRANSFER_PROVIDER_KEY_DATA);
    }
}
