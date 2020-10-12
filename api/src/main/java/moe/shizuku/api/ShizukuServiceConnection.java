package moe.shizuku.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import moe.shizuku.server.IShizukuServiceConnection;

class ShizukuServiceConnection extends IShizukuServiceConnection.Stub {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Map<String, ShizukuServiceConnection> CACHE = Collections.synchronizedMap(new HashMap<>());

    @Nullable
    static ShizukuServiceConnection get(ShizukuService.UserServiceArgs args) {
        String key = args.tag != null ? args.tag : args.componentName.getClassName();
        return CACHE.get(key);
    }

    @NonNull
    static ShizukuServiceConnection getOrCreate(ShizukuService.UserServiceArgs args) {
        String key = args.tag != null ? args.tag : args.componentName.getClassName();
        ShizukuServiceConnection connection = CACHE.get(key);

        if (connection == null) {
            connection = new ShizukuServiceConnection(args);
            CACHE.put(key, connection);
        }
        return connection;
    }

    private final Set<ServiceConnection> connections = new HashSet<>();
    private final ComponentName componentName;
    private final boolean standalone;

    public ShizukuServiceConnection(ShizukuService.UserServiceArgs args) {
        this.componentName = args.componentName;
        this.standalone = args.standalone;
    }

    private boolean dead = false;

    public void addConnection(@Nullable ServiceConnection conn) {
        if (conn != null) {
            connections.add(conn);
        }
    }

    public void removeConnection(@Nullable ServiceConnection conn) {
        if (conn != null) {
            connections.remove(conn);
        }
    }

    @Override
    public void connected(IBinder binder) {
        MAIN_HANDLER.post(() -> {
                    for (ServiceConnection conn : connections) {
                        conn.onServiceConnected(componentName, binder);
                    }
                }
        );

        if (standalone) {
            try {
                binder.linkToDeath(this::dead, 0);
            } catch (RemoteException ignored) {
            }
        }
    }

    @Override
    public void dead() {
        if (dead) return;
        dead = true;

        MAIN_HANDLER.post(() -> {
                    for (ServiceConnection conn : connections) {
                        conn.onServiceDisconnected(componentName);
                    }
                }
        );
    }
}
