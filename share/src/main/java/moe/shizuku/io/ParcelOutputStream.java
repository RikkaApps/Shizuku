package moe.shizuku.io;

import android.graphics.Bitmap;
import android.os.BadParcelableException;
import android.os.IBinder;
import android.os.IInterface;
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

    public final void writeBooleanArray(boolean[] booleans) throws IOException {
        if (booleans == null) {
            writeInt(-1);
            return;
        }

        int size = booleans.length;
        writeInt(size);
        for (boolean b : booleans) {
            writeBoolean(b);
        }
    }

    public final void writeByteArray(byte[] bytes) throws IOException {
        if (bytes == null) {
            writeInt(-1);
            return;
        }

        int size = bytes.length;
        writeInt(size);
        write(bytes);
    }

    public final void writeShoartArray(short[] shorts) throws IOException {
        if (shorts == null) {
            writeInt(-1);
            return;
        }

        int size = shorts.length;
        writeInt(size);
        for (short s : shorts) {
            writeShort(s);
        }
    }

    public final void writeCharArray(char[] chars) throws IOException {
        if (chars == null) {
            writeInt(-1);
            return;
        }

        int size = chars.length;
        writeInt(size);
        for (char c : chars) {
            writeChar(c);
        }
    }

    public final void writeIntArray(int[] ints) throws IOException {
        if (ints == null) {
            writeInt(-1);
            return;
        }

        int size = ints.length;
        writeInt(size);
        for (int i : ints) {
            writeInt(i);
        }
    }

    public final void writeLongArray(long[] longs) throws IOException {
        if (longs == null) {
            writeInt(-1);
            return;
        }

        int size = longs.length;
        writeInt(size);
        for (long l : longs) {
            writeLong(l);
        }
    }

    public final void writeFloatArray(float[] floats) throws IOException {
        if (floats == null) {
            writeInt(-1);
            return;
        }

        int size = floats.length;
        writeInt(size);
        for (float f : floats) {
            writeFloat(f);
        }
    }

    public final void writeDoubleArray(double[] doubles) throws IOException {
        if (doubles == null) {
            writeInt(-1);
            return;
        }

        int size = doubles.length;
        writeInt(size);
        for (double d : doubles) {
            writeDouble(d);
        }
    }

    public final void writeCharSequence(CharSequence cs) throws IOException {
        writeString(cs.toString());
    }

    public final void writeString(String str) throws IOException {
        if (str == null) {
            writeInt(-1);
        } else {
            writeInt(0);
            writeUTF(str);
        }
    }

    public final void writeStringArray(String[] strings) throws IOException {
        if (strings == null) {
            writeInt(-1);
            return;
        }

        int size = strings.length;
        writeInt(size);
        for (String s : strings) {
            writeString(s);
        }
    }

    public void writeStringList(List<String> strings) throws IOException {
        if (strings == null) {
            writeInt(-1);
            return;
        }

        int size = strings.size();
        writeInt(size);
        for (String s : strings) {
            writeString(s);
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

    public final void writeParcelableArray(Parcelable[] parcelables) throws IOException {
        if (parcelables == null) {
            writeInt(-1);
            return;
        }

        int size = parcelables.length;
        writeInt(size);
        for (Parcelable p : parcelables) {
            writeParcelable(p);
        }
    }

    public final void writeParcelableList(List<? extends Parcelable> parcelables) throws IOException {
        if (parcelables == null) {
            writeInt(-1);
            return;
        }

        Parcel parcel = Parcel.obtain();
        parcel.writeTypedList(parcelables);
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

    public final void writeBinder(IBinder binder) throws IOException {
        writeByte(0);
    }

    public final void writeInterface(IInterface _interface) throws IOException {
        writeByte(0);
    }

    public final void writeBinderList(List<IBinder> binders) throws IOException {
        writeByte(0);
    }

    public final void writeInterfaceList(List<IInterface> interfaces) throws IOException {
        writeByte(0);
    }

    public final void writeBinderArray(IBinder[] binders) throws IOException {
        writeByte(0);
    }

    public final void writeInterfaceArray(IInterface[] interfaces) throws IOException {
        writeByte(0);
    }
}
