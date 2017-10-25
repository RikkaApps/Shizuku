package moe.shizuku.server.api;

import android.os.Handler;
import android.os.RemoteException;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.server.ShizukuServer;
import moe.shizuku.server.util.Utils;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuRequestHandler extends RequestHandler {

    private final Handler mHandler;
    private final UUID mToken;

    public ShizukuRequestHandler(Handler handler, UUID token) {
        mHandler = handler;
        mToken = token;
    }

    public void handle(Socket socket) throws IOException, RemoteException {
        ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
        ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
        String action = is.readString();
        if (isActionRequireAuthorization(action)) {
            long most = is.readLong();
            long least = is.readLong();
            if (most != mToken.getMostSignificantBits()
                    && least != mToken.getLeastSignificantBits()) {
                os.writeException(new SecurityException("unauthorized"));
            }
        }

        switch (action) {
            case ShizukuClient.ACTION_GET_VERSION:
                version(os);
                break;
            case ShizukuClient.ACTION_REQUEST_STOP:
                stop(os, mHandler);
                break;
            case ShizukuClient.ACTION_AUTHORIZE:
                authorize(is, os, mToken);
                break;
            case ShizukuClient.ACTION_SEND_TOKEN:
                sendTokenToManger(is, os, mToken);
                break;
            default:
                handle(action, is, os);
                break;
        }

        is.close();
        os.flush();
        os.close();
    }

    private static boolean isActionRequireAuthorization(String action) {
        return !Objects.equals(action, ShizukuClient.ACTION_GET_VERSION)
                && !Objects.equals(action, ShizukuClient.ACTION_AUTHORIZE)
                && !Objects.equals(action, ShizukuClient.ACTION_REQUEST_STOP)
                && !Objects.equals(action, ShizukuClient.ACTION_SEND_TOKEN);
    }

    public static void version(ParcelOutputStream os) throws RemoteException, IOException {
        os.writeNoException();
        if (Utils.isServerDead()) {
            os.writeParcelable(ShizukuState.createServerDead());
        } else {
            os.writeParcelable(ShizukuState.createOk());
        }
    }

    public static void authorize(ParcelInputStream is, ParcelOutputStream os, UUID token) throws RemoteException, IOException {
        long most = is.readLong();
        long least = is.readLong();

        os.writeNoException();
        if (Utils.isServerDead()) {
            os.writeParcelable(ShizukuState.createServerDead());
        } else if (most != token.getMostSignificantBits()
                && least != token.getLeastSignificantBits()) {
            os.writeParcelable(ShizukuState.createUnauthorized());
        } else {
            os.writeParcelable(ShizukuState.createOk());
        }
    }

    private static void stop(ParcelOutputStream os, Handler handler) throws IOException {
        os.writeNoException();

        handler.sendEmptyMessage(ShizukuServer.MESSAGE_EXIT);
    }

    private static void sendTokenToManger(ParcelInputStream is, ParcelOutputStream os, UUID token) throws IOException, RemoteException {
        int uid = is.readInt();
        int userId = uid / 100000;

        ShizukuServer.sendTokenToManger(token, userId);

        os.writeNoException();
    }
}
