package moe.shizuku.server;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.net.InetAddress;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Rikka on 2017/5/4.
 */

public class Protocol implements Parcelable  {

    public static final int PORT = 55608;
    public static final InetAddress HOST = InetAddress.getLoopbackAddress();

    public static final int VERSION = BuildConfig.VERSION_CODE;

    protected int mVersion;
    protected boolean mIsRoot;
    protected int mCode;

    @Retention(SOURCE)
    @IntDef({RESULT_OK, RESULT_UNAUTHORIZED, RESULT_SERVER_DEAD, RESULT_UNKNOWN})
    public @interface ResultCode {}
    public static final int RESULT_OK = 0;
    public static final int RESULT_UNAUTHORIZED = 1;
    public static final int RESULT_SERVER_DEAD = 2;
    public static final int RESULT_UNKNOWN = 3;

    public Protocol(@ResultCode int code) {
        mVersion = VERSION;
        mIsRoot = HideApiOverride.isRoot(Process.myUid());
        mCode = code;
    }

    public final boolean versionUnmatched() {
        return mVersion != VERSION;
    }

    public int getVersion() {
        return mVersion;
    }

    public boolean isRoot() {
        return mIsRoot;
    }

    public @ResultCode int getCode() {
        return mCode;
    }

    protected Protocol(Parcel in) {
        mVersion = in.readInt();
        mIsRoot = in.readByte() != 0;
        mCode = in.readInt();
    }

    public static Protocol createUnknown() {
        return new Protocol(RESULT_UNKNOWN);
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

    public static final Creator<Protocol> CREATOR = new Creator<Protocol>() {
        @Override
        public Protocol createFromParcel(Parcel in) {
            return new Protocol(in);
        }

        @Override
        public Protocol[] newArray(int size) {
            return new Protocol[size];
        }
    };

    @Override
    public String toString() {
        return "Protocol{" +
                "mVersion=" + mVersion +
                ", mIsRoot=" + mIsRoot +
                ", mCode=" + mCode +
                '}';
    }
}
