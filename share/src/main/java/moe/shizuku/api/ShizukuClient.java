package moe.shizuku.api;

import android.os.Process;

import java.net.Socket;
import java.util.UUID;

import moe.shizuku.ShizukuConfiguration;
import moe.shizuku.ShizukuState;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuClient {

    public static final int ACTION_GET_VERSION = 1;
    public static final int ACTION_REQUEST_STOP = 2;
    public static final int ACTION_AUTHORIZE = 3;
    public static final int ACTION_SEND_TOKEN = 4;

    private static UUID sToken = new UUID(0, 0);

    public static UUID getToken() {
        return sToken;
    }

    public static void setToken(UUID token) {
        sToken = token;
    }

    public static ShizukuState getVersion() {
        try {
            Socket client = new Socket(ShizukuConfiguration.HOST, ShizukuConfiguration.PORT);
            client.setSoTimeout(ShizukuConfiguration.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(ACTION_GET_VERSION);
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception ignored) {
        }
        return ShizukuState.createUnknown();
    }

    public static ShizukuState authorize(UUID token) {
        try {
            Socket client = new Socket(ShizukuConfiguration.HOST, ShizukuConfiguration.PORT);
            client.setSoTimeout(ShizukuConfiguration.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(ACTION_AUTHORIZE);
            os.writeLong(token.getMostSignificantBits());
            os.writeLong(token.getLeastSignificantBits());
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception ignored) {
        }
        return ShizukuState.createUnknown();
    }

    public static boolean stopServer() {
        try {
            Socket socket = new Socket(ShizukuConfiguration.HOST, ShizukuConfiguration.PORT);
            socket.setSoTimeout(ShizukuConfiguration.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeInt(ACTION_REQUEST_STOP);
            is.readException();

            //ServerLog.i("send stop to old server");
            return true;
        } catch (Exception e) {
            //ServerLog.i("cannot connect to old server, maybe it not exists");
            return false;
        }
    }

    public static void sendTokenToManager() {
        try {
            Socket socket = new Socket(ShizukuConfiguration.HOST, ShizukuConfiguration.PORT);
            socket.setSoTimeout(ShizukuConfiguration.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeInt(ACTION_SEND_TOKEN);
            os.writeInt(Process.myUid());
            is.readException();
        } catch (Exception ignored) {
        }
    }
}
