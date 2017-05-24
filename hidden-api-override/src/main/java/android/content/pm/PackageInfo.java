package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rikka on 2017/5/16.
 */

public class PackageInfo implements Parcelable {

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }

    public static final Creator<PackageInfo> CREATOR = new Creator<PackageInfo>() {
        @Override
        public PackageInfo createFromParcel(Parcel in) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PackageInfo[] newArray(int size) {
            throw new UnsupportedOperationException();
        }
    };
}
