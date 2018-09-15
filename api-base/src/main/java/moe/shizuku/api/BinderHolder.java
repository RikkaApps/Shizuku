package moe.shizuku.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class BinderHolder implements Parcelable {

    public IBinder binder;

    public BinderHolder(IBinder binder) {
        this.binder = binder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.binder);
    }

    protected BinderHolder(Parcel in) {
        this.binder = in.readStrongBinder();
    }

    public static final Creator<BinderHolder> CREATOR = new Creator<BinderHolder>() {
        @Override
        public BinderHolder createFromParcel(Parcel source) {
            return new BinderHolder(source);
        }

        @Override
        public BinderHolder[] newArray(int size) {
            return new BinderHolder[size];
        }
    };
}
