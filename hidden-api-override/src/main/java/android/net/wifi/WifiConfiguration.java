package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rikka on 2017/5/15.
 */

public class WifiConfiguration implements Parcelable {

    protected WifiConfiguration(Parcel in) {
        throw new UnsupportedOperationException();
    }

    public static final Creator<WifiConfiguration> CREATOR = new Creator<WifiConfiguration>() {
        @Override
        public WifiConfiguration createFromParcel(Parcel in) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WifiConfiguration[] newArray(int size) {
            throw new UnsupportedOperationException();
        }
    };

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }
}
