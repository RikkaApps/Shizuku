package moe.shizuku.manager.legacy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BadParcelableException;
import android.os.NetworkOnMainThreadException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.ShizukuManagerSettings;

/**
 * Copied and minimized from Shizuku v2 API.
 */
public class ShizukuLegacy {

    private static final String TAG = "ShizukuLegacy";

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";

    public static final int SERVER_VERSION = 32;
    public static final int MAX_SDK = 27;

    public static final String ACTION_UPDATE_TOKEN = MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN";

    public static final String EXTRA_TOKEN_MOST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    public static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";

    public static UUID getToken() {
        final SharedPreferences preferences = ShizukuManagerSettings.getPreferences();
        long mostSig = preferences.getLong("token_most", 0);
        long leastSig = preferences.getLong("token_least", 0);
        return new UUID(mostSig, leastSig);
    }

    public static void putToken(Intent intent) {
        long mostSig = intent.getLongExtra(EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(EXTRA_TOKEN_LEAST_SIG, 0);

        UUID token = new UUID(mostSig, leastSig);
        putToken(token);
    }

    public static void putToken(UUID token) {
        long mostSig = token.getMostSignificantBits();
        long leastSig = token.getLeastSignificantBits();

        SharedPreferences preferences = ShizukuManagerSettings.getPreferences();
        preferences.edit()
                .putLong("token_most", mostSig)
                .putLong("token_least", leastSig)
                .apply();

        Log.i(AppConstants.TAG, "legacy token update: " + token);
    }

    public final static class ShizukuState implements Parcelable {

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

        public static ShizukuState createUnknown() {
            return new ShizukuState(STATUS_UNKNOWN);
        }

        private ShizukuState(int code) {
            mVersion = SERVER_VERSION;
            mIsRoot = Process.myUid() == 0;
            mCode = code;
        }

        /**
         * Returns whether the version number at compile time is not the same as the server version number.
         *
         * @return version unmatched
         */
        public boolean versionUnmatched() {
            return mVersion != SERVER_VERSION;
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
         * @return status code
         * @see ShizukuState#STATUS_AUTHORIZED
         * @see ShizukuState#STATUS_UNAUTHORIZED
         * @see ShizukuState#STATUS_UNAVAILABLE
         * @see ShizukuState#STATUS_UNKNOWN
         */
        public int getCode() {
            return mCode;
        }

        public ShizukuState(Parcel in) {
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

    public static class ShizukuClient {


        private static final String ACTION_AUTHORIZE = "Shizuku_authorize";

        /**
         * Activity result: ok, token is returned.
         */
        public static final int AUTH_RESULT_OK = Activity.RESULT_OK;

        /**
         * Activity result: user denied request (only API pre-23).
         */
        public static final int AUTH_RESULT_USER_DENIED = Activity.RESULT_CANCELED;

        /**
         * Activity result: error, such as manager app itself not authorized.
         */
        public static final int AUTH_RESULT_ERROR = 1;

        /**
         * Return a {@link ShizukuState} instance that describes server status and if the client is
         * authorized.
         *
         * @return status
         * @see ShizukuState#isAuthorized()
         * @see ShizukuState#isRoot()
         * @see ShizukuState#isServerAvailable()
         */
        public static ShizukuState getState() {
            return getState(getToken());
        }

        private static final int PORT = 55609;
        private static final InetAddress HOST = InetAddress.getLoopbackAddress();
        private static final int TIMEOUT = 10000;

        private static final int EX_SECURITY = -1;
        private static final int EX_BAD_PARCELABLE = -2;
        private static final int EX_ILLEGAL_ARGUMENT = -3;
        private static final int EX_NULL_POINTER = -4;
        private static final int EX_ILLEGAL_STATE = -5;
        private static final int EX_NETWORK_MAIN_THREAD = -6;
        private static final int EX_UNSUPPORTED_OPERATION = -7;
        private static final int EX_SERVICE_SPECIFIC = -8;
        private static final int EX_UNKNOWN = -128;

        private static ShizukuState getState(UUID token) {
            DataOutputStream os = null;
            DataInputStream is = null;
            try {
                Socket client = new Socket(HOST, PORT);
                client.setSoTimeout(TIMEOUT);

                os = new DataOutputStream(client.getOutputStream());
                is = new DataInputStream(client.getInputStream());
                os.writeInt(0);
                os.writeUTF(ACTION_AUTHORIZE);
                os.writeLong(token.getMostSignificantBits());
                os.writeLong(token.getLeastSignificantBits());

                // is.readException
                int code = is.readInt();
                if (code != 0) {
                    String msg = is.readInt() == 0 ? is.readUTF() : null;
                    switch (code) {
                        case EX_SECURITY:
                            throw new SecurityException(msg);
                        case EX_BAD_PARCELABLE:
                            throw new BadParcelableException(msg);
                        case EX_ILLEGAL_ARGUMENT:
                            throw new IllegalArgumentException(msg);
                        case EX_NULL_POINTER:
                            throw new NullPointerException(msg);
                        case EX_ILLEGAL_STATE:
                            throw new IllegalStateException(msg);
                        case EX_NETWORK_MAIN_THREAD:
                            throw new NetworkOnMainThreadException();
                        case EX_UNSUPPORTED_OPERATION:
                            throw new UnsupportedOperationException(msg);
                    }
                    throw new RuntimeException(msg);
                }

                // is.readBytes
                byte[] bytes;
                int size = is.readInt();
                if (size == -1) {
                    bytes = null;
                } else {
                    bytes = new byte[size];
                    int length;
                    int offset = 0;
                    int remain = bytes.length;
                    while (remain > 0 && (length = is.read(bytes, offset, remain)) != -1) {
                        if (length > 0) {
                            offset += length;
                            remain -= length;
                        }
                    }
                }

                // is.readParcelable
                if (bytes == null) {
                    return null;
                }

                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                try {
                    Constructor constructor = ShizukuState.class.getConstructor(Parcel.class);
                    ShizukuState result = (ShizukuState) constructor.newInstance(parcel);
                    parcel.recycle();
                    return result;
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    return null;
                }
            } catch (Exception e) {
                Log.w(TAG, "can't connect to server: " + e.getMessage());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return ShizukuState.createUnknown();
        }
    }
}
