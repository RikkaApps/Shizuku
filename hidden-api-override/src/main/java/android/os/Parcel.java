package android.os;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rikka on 2017/5/11.
 */

public class Parcel {

    public final void writeByteArray(byte[] b) {
        throw new UnsupportedOperationException();
    }

    public final void writeByteArray(byte[] b, int offset, int len) {
        throw new UnsupportedOperationException();
    }

    public final void writeBlob(byte[] b) {
        throw new UnsupportedOperationException();
    }

    public final void writeBlob(byte[] b, int offset, int len) {
        throw new UnsupportedOperationException();
    }

    public final void writeInt(int val) {
        throw new UnsupportedOperationException();
    }

    public final void writeLong(long val) {
        throw new UnsupportedOperationException();
    }

    public final void writeFloat(float val) {
        throw new UnsupportedOperationException();
    }

    public final void writeDouble(double val) {
        throw new UnsupportedOperationException();
    }

    public final void writeString(String val) {
        throw new UnsupportedOperationException();
    }

    public final void writeCharSequence(CharSequence val) {
        throw new UnsupportedOperationException();
    }

    /*public final void writeStrongBinder(IBinder val) {
        throw new UnsupportedOperationException();
    }*/

    /*public final void writeStrongInterface(IInterface val) {
        throw new UnsupportedOperationException();
    }*/

    public final void writeFileDescriptor(FileDescriptor val) {
        throw new UnsupportedOperationException();
    }

    public final void writeRawFileDescriptor(FileDescriptor val) {
        throw new UnsupportedOperationException();
    }

    public final void writeRawFileDescriptorArray(FileDescriptor[] value) {
        throw new UnsupportedOperationException();
    }

    public final void writeByte(byte val) {
        throw new UnsupportedOperationException();
    }

    public final void writeMap(Map val) {
        throw new UnsupportedOperationException();
    }

    /*public void writeArraySet(@Nullable ArraySet<? extends Object> val) {
        throw new UnsupportedOperationException();
    }*/

    /*public final void writeBundle(Bundle val) {
        if (val == null) {
            writeInt(-1);
            return;
        }

        val.writeToParcel(this, 0);
    }*/

    /*public final void writePersistableBundle(PersistableBundle val) {
        throw new UnsupportedOperationException();
    }*/

    /*public final void writeSize(Size val) {
        throw new UnsupportedOperationException();
    }*/

    /*public final void writeSizeF(SizeF val) {
        throw new UnsupportedOperationException();
    }*/

    public final void writeList(List val) {
        throw new UnsupportedOperationException();
    }

    public final void writeArray(Object[] val) {
        throw new UnsupportedOperationException();
    }
    /*public final void writeSparseArray(SparseArray<Object> val) {
        throw new UnsupportedOperationException();
    }

    public final void writeSparseBooleanArray(SparseBooleanArray val) {
        throw new UnsupportedOperationException();
    }*/

    public final void writeBooleanArray(boolean[] val) {
        throw new UnsupportedOperationException();
    }

    public final boolean[] createBooleanArray() {
        throw new UnsupportedOperationException();
    }

    public final void readBooleanArray(boolean[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeCharArray(char[] val) {
        throw new UnsupportedOperationException();
    }

    public final char[] createCharArray() {
        throw new UnsupportedOperationException();
    }

    public final void readCharArray(char[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeIntArray(int[] val) {
        throw new UnsupportedOperationException();
    }

    public final int[] createIntArray() {
        throw new UnsupportedOperationException();
    }

    public final void readIntArray(int[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeLongArray(long[] val) {
        throw new UnsupportedOperationException();
    }

    public final long[] createLongArray() {
        throw new UnsupportedOperationException();
    }

    public final void readLongArray(long[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeFloatArray(float[] val) {
        throw new UnsupportedOperationException();
    }

    public final float[] createFloatArray() {
        throw new UnsupportedOperationException();
    }

    public final void readFloatArray(float[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeDoubleArray(double[] val) {
        throw new UnsupportedOperationException();
    }

    public final double[] createDoubleArray() {
        throw new UnsupportedOperationException();
    }

    public final void readDoubleArray(double[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeStringArray(String[] val) {
        throw new UnsupportedOperationException();
    }

    public final String[] createStringArray() {
        throw new UnsupportedOperationException();
    }

    public final void readStringArray(String[] val) {
        throw new UnsupportedOperationException();
    }

    /*public final void writeBinderArray(IBinder[] val) {
        throw new UnsupportedOperationException();
    }*/

    public final void writeCharSequenceArray(CharSequence[] val) {
        throw new UnsupportedOperationException();
    }

    public final void writeCharSequenceList(ArrayList<CharSequence> val) {
        throw new UnsupportedOperationException();
    }

    /*public final IBinder[] createBinderArray() {
        throw new UnsupportedOperationException();
    }*/

    /*public final void readBinderArray(IBinder[] val) {
        throw new UnsupportedOperationException();
    }*/

    public final <T extends Parcelable> void writeTypedList(List<T> val) {
        throw new UnsupportedOperationException();
    }

    public final void writeStringList(List<String> val) {
        throw new UnsupportedOperationException();
    }

    /*public final void writeBinderList(List<IBinder> val) {
        throw new UnsupportedOperationException();
    }*/

    public final <T extends Parcelable> void writeTypedArray(T[] val,
                                                             int parcelableFlags) {
        throw new UnsupportedOperationException();
    }

    public final <T extends Parcelable> void writeTypedObject(T val, int parcelableFlags) {
        throw new UnsupportedOperationException();
    }

    public final void writeValue(Object v) {
        throw new UnsupportedOperationException();
    }

    public final void writeParcelable(Parcelable p, int parcelableFlags) {
        throw new UnsupportedOperationException();
    }

    public final void writeParcelableCreator(Parcelable p) {
        throw new UnsupportedOperationException();
    }

    public final void writeSerializable(Serializable s) {
        throw new UnsupportedOperationException();
    }

    public final void writeException(Exception e) {
        throw new UnsupportedOperationException();
    }

    public final void writeNoException() {
        throw new UnsupportedOperationException();
    }
    public final void readException() {
        throw new UnsupportedOperationException();
    }

    public final int readExceptionCode() {
        throw new UnsupportedOperationException();
    }

    public final void readException(int code, String msg) {
        throw new UnsupportedOperationException();
    }

    public byte readByte() {
        throw new UnsupportedOperationException();
    }

    public final int readInt() {
        throw new UnsupportedOperationException();
    }

    public final long readLong() {
        throw new UnsupportedOperationException();
    }

    public final float readFloat() {
        throw new UnsupportedOperationException();
    }

    public final double readDouble() {
        throw new UnsupportedOperationException();
    }

    public final String readString() {
        throw new UnsupportedOperationException();
    }

    public final CharSequence readCharSequence() {
        throw new UnsupportedOperationException();
    }

    /*public final ParcelFileDescriptor readFileDescriptor() {
        throw new UnsupportedOperationException();
    }*/

    public final FileDescriptor readRawFileDescriptor() {
        throw new UnsupportedOperationException();
    }

    public final FileDescriptor[] createRawFileDescriptorArray() {
        throw new UnsupportedOperationException();

    }
    public void readRawFileDescriptorArray(FileDescriptor[] val) {
        throw new UnsupportedOperationException();
    }

    public final <T extends Parcelable> T readParcelable(ClassLoader loader) {
        throw new UnsupportedOperationException();
    }
}
