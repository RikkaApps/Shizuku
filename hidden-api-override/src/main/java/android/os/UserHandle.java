package android.os;

/**
 * Created by Rikka on 2017/5/11.
 */

public class UserHandle implements Parcelable {

    public int getIdentifier() {
        throw new UnsupportedOperationException();
    }

    public static final Creator<UserHandle> CREATOR = new Creator<UserHandle>() {
        @Override
        public UserHandle createFromParcel(Parcel in) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserHandle[] newArray(int size) {
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
