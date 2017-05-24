package hidden.android.content.pm;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rikka on 2017/5/16.
 */

public class WrappedPackageInfo implements Parcelable {

    private PackageInfo mWrapped;
    public int flags;

    public WrappedPackageInfo(PackageInfo wrapped, Parcel source) {
        mWrapped = wrapped;
        flags = source.readInt();
    }

    public PackageInfo getWrapped() {
        return mWrapped;
    }

    public static final android.os.Parcelable.Creator<WrappedPackageInfo> CREATOR
            = new android.os.Parcelable.Creator<WrappedPackageInfo>() {
        @Override
        public WrappedPackageInfo createFromParcel(Parcel source) {
            return new WrappedPackageInfo(PackageInfo.CREATOR.createFromParcel(source), source);
        }

        @Override
        public WrappedPackageInfo[] newArray(int size) {
            return new WrappedPackageInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return mWrapped.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mWrapped.writeToParcel(dest, flags);
        dest.writeInt(flags);
    }
}
