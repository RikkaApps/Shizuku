package moe.shizuku;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;

/**
 * Created by Rikka on 2017/5/4.
 */

public final class ShizukuState implements Parcelable {

    /**
     * Server is running and client is authorized.
     */
    public static final int STATUS_AUTHORIZED = 1;

    /**
     * Server is running but client is not authorized.
     */
    public static final int STATUS_UNAUTHORIZED = 2;

    /**
     * The server process is running but Android system is not ready.
     */
    public static final int STATUS_UNAVAILABLE = 3;

    /**
     * Server is not running or cannot communicate with server.
     */
    public static final int STATUS_UNKNOWN = 4;

    private int mVersion;
    private boolean mIsRoot;
    private int mCode;

    public static ShizukuState createAuthorized() {
        return new ShizukuState(STATUS_AUTHORIZED);
    }

    public static ShizukuState createUnknown() {
        return new ShizukuState(STATUS_UNKNOWN);
    }

    public static ShizukuState createUnavailable() {
        return new ShizukuState(STATUS_UNAVAILABLE);
    }

    public static ShizukuState createUnauthorized() {
        return new ShizukuState(STATUS_UNAUTHORIZED);
    }

    private ShizukuState(int code) {
        mVersion = ShizukuConstants.SERVER_VERSION;
        mIsRoot = Process.myUid() == Process.ROOT_UID;
        mCode = code;
    }

    /**
     * Returns whether the version number at compile time is not the same as the server version number.
     *
     * @return version unmatched
     */
    public boolean versionUnmatched() {
        return mVersion != ShizukuConstants.SERVER_VERSION;
    }

    /**
     * Return current running server version.
     *
     * @return server version
     */
    public int getVersion() {
        return mVersion;
    }

    /**
     * Return current running server is in root user.
     *
     * @return server is root
     */
    public boolean isRoot() {
        return mIsRoot;
    }

    /**
     * Return if the server is running and available.
     *
     * @return is server running and available
     */
    public boolean isServerAvailable() {
        return mCode == STATUS_UNAUTHORIZED || mCode == STATUS_AUTHORIZED;
    }

    /**
     * Return if the server is available and client is authorized.
     *
     * @return client is authorized and server available
     */
    public boolean isAuthorized() {
        return mCode == STATUS_AUTHORIZED;
    }

    /**
     * Return status code.
     *
     * @see ShizukuState#STATUS_AUTHORIZED
     * @see ShizukuState#STATUS_UNAUTHORIZED
     * @see ShizukuState#STATUS_UNAVAILABLE
     * @see ShizukuState#STATUS_UNKNOWN
     *
     * @return status code
     */
    public int getCode() {
        return mCode;
    }

    protected ShizukuState(Parcel in) {
        mVersion = in.readInt();
        mIsRoot = in.readByte() != 0;
        mCode = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mVersion);
        dest.writeByte(mIsRoot ? (byte) 1 : (byte) 0);
        dest.writeInt(mCode);
    }

    public static final Creator<ShizukuState> CREATOR = new Creator<ShizukuState>() {
        @Override
        public ShizukuState createFromParcel(Parcel in) {
            return new ShizukuState(in);
        }

        @Override
        public ShizukuState[] newArray(int size) {
            return new ShizukuState[size];
        }
    };

    @Override
    public String toString() {
        return "ShizukuState{" +
                "mVersion=" + mVersion +
                ", mIsRoot=" + mIsRoot +
                ", mCode=" + mCode +
                '}';
    }
}
