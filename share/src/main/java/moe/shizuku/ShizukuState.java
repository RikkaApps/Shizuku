package moe.shizuku;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;

/**
 * Created by Rikka on 2017/5/4.
 */

public final class ShizukuState implements Parcelable {

    public static final int RESULT_OK = 0;
    public static final int RESULT_UNAUTHORIZED = 1;
    public static final int RESULT_SERVER_DEAD = 2;
    public static final int RESULT_UNKNOWN = 3;

    protected int mVersion;
    protected boolean mIsRoot;
    protected int mCode;

    public static ShizukuState createOk() {
        return new ShizukuState(RESULT_OK);
    }

    public static ShizukuState createUnknown() {
        return new ShizukuState(RESULT_UNKNOWN);
    }

    public static ShizukuState createServerDead() {
        return new ShizukuState(RESULT_SERVER_DEAD);
    }

    public static ShizukuState createUnauthorized() {
        return new ShizukuState(RESULT_UNAUTHORIZED);
    }

    public ShizukuState(int code) {
        mVersion = ShizukuConstants.VERSION;
        mIsRoot = Process.myUid() == Process.ROOT_UID;
        mCode = code;
    }

    public boolean versionUnmatched() {
        return mVersion != ShizukuConstants.VERSION;
    }

    public int getVersion() {
        return mVersion;
    }

    public boolean isRoot() {
        return mIsRoot;
    }

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
