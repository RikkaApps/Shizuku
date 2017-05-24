package hidden.android.net.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Rikka on 2017/5/15.
 */

@SuppressWarnings("unchecked")
public class WrappedWifiConfiguration implements Parcelable {

    private WifiConfiguration mWrapped;

    public WrappedWifiConfiguration(WifiConfiguration wrapped) {
        mWrapped = wrapped;
    }

    public WifiConfiguration getWrapped() {
        return mWrapped;
    }

    private static android.os.Parcelable.Creator<WifiConfiguration> creator;
    private static Method createFromParcel;

    static {
        try {
            creator = (android.os.Parcelable.Creator<WifiConfiguration>) WifiConfiguration.class.getField("CREATOR").get(null);
            createFromParcel = creator.getClass().getMethod("createFromParcel", Parcel.class);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException ignored) {
            creator = null;
            createFromParcel = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mWrapped.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return mWrapped.describeContents();
    }

    public static final android.os.Parcelable.Creator<WrappedWifiConfiguration> CREATOR
            = new android.os.Parcelable.Creator<WrappedWifiConfiguration>() {
        @Override
        public WrappedWifiConfiguration createFromParcel(Parcel source) {
            try {
                return new WrappedWifiConfiguration((WifiConfiguration) createFromParcel.invoke(creator, source));
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }

            return null;
        }

        @Override
        public WrappedWifiConfiguration[] newArray(int size) {
            return new WrappedWifiConfiguration[size];
        }
    };

    @Override
    public String toString() {
        return mWrapped.toString();
    }
}
