package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Rikka on 2017/5/16.
 */

public class ParceledListSlice<T> implements Parcelable {

    public List<T> getList() {
        throw new UnsupportedOperationException();
    }

    protected ParceledListSlice(Parcel in) {
        throw new UnsupportedOperationException();
    }

    public ParceledListSlice(List<T> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR =
            new Parcelable.ClassLoaderCreator<ParceledListSlice>() {
                public ParceledListSlice createFromParcel(Parcel in) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
                    throw new UnsupportedOperationException();
                }
                public ParceledListSlice[] newArray(int size) {
                    throw new UnsupportedOperationException();
                }
            };
}