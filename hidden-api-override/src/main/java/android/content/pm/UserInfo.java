package android.content.pm;

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
    public String iconPath;
    public int flags;
    public long creationTime;
    public long lastLoggedInTime;
    public int profileGroupId;

    public boolean partial;
    public boolean guestToRemove;

    public boolean isPrimary() {
        throw new UnsupportedOperationException();
    }

    public boolean isAdmin() {
        throw new UnsupportedOperationException();
    }

    public boolean isGuest() {
        throw new UnsupportedOperationException();
    }

    public boolean isRestricted() {
        throw new UnsupportedOperationException();
    }

    public boolean isManagedProfile() {
        throw new UnsupportedOperationException();
    }

    public boolean isEnabled() {
        throw new UnsupportedOperationException();
    }

    public boolean isQuietModeEnabled() {
        throw new UnsupportedOperationException();
    }

    public boolean isEphemeral() {
        throw new UnsupportedOperationException();
    }

    public boolean isInitialized() {
        throw new UnsupportedOperationException();
    }

    public boolean isDemo() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the user is a split system user.
     * <p>If {@link UserManager#isSplitSystemUser split system user mode} is not enabled,
     * the method always returns false.
     */
    public boolean isSystemOnly() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the given user is a split system user.
     * <p>If {@link UserManager#isSplitSystemUser split system user mode} is not enabled,
     * the method always returns false.
     */
    public static boolean isSystemOnly(int userId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if this user can be switched to.
     **/
    public boolean supportsSwitchTo() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if this user can be switched to by end user through UI.
     */
    public boolean supportsSwitchToByUser() {
        throw new UnsupportedOperationException();
    }

    public boolean canHaveProfile() {
        throw new UnsupportedOperationException();
    }

    protected UserInfo(Parcel in) {
        throw new UnsupportedOperationException();
    }

    public UserHandle getUserHandle() {
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

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserInfo[] newArray(int size) {
            throw new UnsupportedOperationException();
        }
    };
}
