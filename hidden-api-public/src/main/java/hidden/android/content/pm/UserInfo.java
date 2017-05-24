package hidden.android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

/**
 * Created by Rikka on 2017/5/8.
 */

public class UserInfo implements Parcelable {

    public int id;
    public int serialNumber;
    public String name;

    private boolean isPrimary;
    private boolean isAdmin;
    private boolean isGuest;
    private boolean isRestricted;
    private boolean isManagedProfile;
    private boolean isEnabled;

    private UserHandle mUserHandle;

    public UserInfo(int id, int serialNumber, String name, boolean isPrimary, boolean isAdmin, boolean isGuest, boolean isRestricted, boolean isManagedProfile, boolean isEnabled, UserHandle userHandle) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.name = name;
        this.isPrimary = isPrimary;
        this.isAdmin = isAdmin;
        this.isGuest = isGuest;
        this.isRestricted = isRestricted;
        this.isManagedProfile = isManagedProfile;
        this.isEnabled = isEnabled;
        this.mUserHandle = userHandle;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public boolean isManagedProfile() {
        return isManagedProfile;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public UserHandle getUserHandle() {
        return mUserHandle;
    }

    @Override
    public String toString() {
        return "UserInfo{" + id + ":" + name + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.serialNumber);
        dest.writeString(this.name);
        dest.writeByte(this.isPrimary ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAdmin ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isGuest ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isRestricted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isManagedProfile ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isEnabled ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.mUserHandle, flags);
    }

    protected UserInfo(Parcel in) {
        this.id = in.readInt();
        this.serialNumber = in.readInt();
        this.name = in.readString();
        this.isPrimary = in.readByte() != 0;
        this.isAdmin = in.readByte() != 0;
        this.isGuest = in.readByte() != 0;
        this.isRestricted = in.readByte() != 0;
        this.isManagedProfile = in.readByte() != 0;
        this.isEnabled = in.readByte() != 0;
        this.mUserHandle = in.readParcelable(UserHandle.class.getClassLoader());
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
