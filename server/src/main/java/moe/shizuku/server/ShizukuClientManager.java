package moe.shizuku.server;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import moe.shizuku.server.config.Config;
import moe.shizuku.server.config.ShizukuConfigManager;
import rikka.shizuku.server.ClientManager;
import rikka.shizuku.server.ClientRecord;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuClientManager extends ClientManager<ShizukuConfigManager> {

    public ShizukuClientManager(ShizukuConfigManager configManager) {
        super(configManager);
    }

    private final List<ClientRecord> clientRecords = Collections.synchronizedList(new ArrayList<>());

    public List<ClientRecord> findClients(int uid) {
        synchronized (this) {
            List<ClientRecord> res = new ArrayList<>();
            for (ClientRecord clientRecord : clientRecords) {
                if (clientRecord.uid == uid) {
                    res.add(clientRecord);
                }
            }
            return res;
        }
    }

    public ClientRecord findClient(int uid, int pid) {
        for (ClientRecord clientRecord : clientRecords) {
            if (clientRecord.pid == pid && clientRecord.uid == uid) {
                return clientRecord;
            }
        }
        return null;
    }

    public ClientRecord addClient(int uid, int pid, IShizukuApplication client, String packageName) {
        ClientRecord clientRecord = new ClientRecord(uid, pid, client, packageName);

        Config.PackageEntry entry = getConfigManager().find(uid);
        if (entry != null && entry.isAllowed()) {
            clientRecord.allowed = true;
        }

        IBinder binder = client.asBinder();
        IBinder.DeathRecipient deathRecipient = (IBinder.DeathRecipient) () -> clientRecords.remove(clientRecord);
        try {
            binder.linkToDeath(deathRecipient, 0);
        } catch (RemoteException e) {
            LOGGER.w(e, "addClient: linkToDeath failed");
            return null;
        }

        clientRecords.add(clientRecord);
        return clientRecord;
    }
}


