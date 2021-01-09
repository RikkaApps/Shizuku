package moe.shizuku.server.config;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AtomicFile;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import kotlin.collections.ArraysKt;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.SystemService;
import moe.shizuku.server.ktx.HandlerKt;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ConfigManager {

    private static final Gson GSON_IN = new GsonBuilder()
            .create();
    private static final Gson GSON_OUT = new GsonBuilder()
            .setVersion(Config.LATEST_VERSION)
            .create();

    private static final long WRITE_DELAY = 10 * 1000;

    private static final File FILE = new File("/data/local/tmp/shizuku/shizuku.json");
    private static final AtomicFile ATOMIC_FILE = new AtomicFile(FILE);

    public static Config load() {
        FileInputStream stream;
        try {
            stream = ATOMIC_FILE.openRead();
        } catch (FileNotFoundException e) {
            LOGGER.i("no existing config file " + ATOMIC_FILE.getBaseFile() + "; starting empty");
            return new Config();
        }

        Config config = null;
        try {
            config = GSON_IN.fromJson(new InputStreamReader(stream), Config.class);
        } catch (Throwable tr) {
            LOGGER.w(tr, "load config");
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.w("failed to close: " + e);
            }
        }
        return config;
    }

    public static void write(Config config) {
        synchronized (ATOMIC_FILE) {
            FileOutputStream stream;
            try {
                stream = ATOMIC_FILE.startWrite();
            } catch (IOException e) {
                LOGGER.w("failed to write state: " + e);
                return;
            }

            try {
                String json = GSON_OUT.toJson(config);
                stream.write(json.getBytes());

                ATOMIC_FILE.finishWrite(stream);
                LOGGER.v("config saved");
            } catch (Throwable tr) {
                LOGGER.w(tr, "can't save %s, restoring backup.", ATOMIC_FILE.getBaseFile());
                ATOMIC_FILE.failWrite(stream);
            }
        }
    }

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private final Runnable mWriteRunner = new Runnable() {

        @Override
        public void run() {
            write(config);
        }
    };

    private final Config config;

    private ConfigManager() {
        this.config = load();

        for (int userId : SystemService.getUserIdsNoThrow()) {
            for (PackageInfo pi : SystemService.getInstalledPackagesNoThrow(PackageManager.GET_PERMISSIONS, userId)) {
                if (pi == null
                        || pi.applicationInfo == null
                        || pi.requestedPermissions == null
                        || !ArraysKt.contains(pi.requestedPermissions, ShizukuApiConstants.PERMISSION)) {
                    continue;
                }

                int uid = pi.applicationInfo.uid;
                boolean allowed;
                try {
                    allowed = SystemService.checkPermission(ShizukuApiConstants.PERMISSION, uid) == PackageManager.PERMISSION_GRANTED;
                } catch (Throwable e) {
                    LOGGER.w("checkPermission");
                    continue;
                }

                updateLocked(uid, Config.MASK_PERMISSION, allowed ? Config.FLAG_ALLOWED : 0);
            }
        }
    }

    private void scheduleWriteLocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (HandlerKt.getWorkerHandler().hasCallbacks(mWriteRunner)) {
                return;
            }
        } else {
            HandlerKt.getWorkerHandler().removeCallbacks(mWriteRunner);
        }
        HandlerKt.getWorkerHandler().postDelayed(mWriteRunner, WRITE_DELAY);
    }

    private Config.PackageEntry findLocked(int uid) {
        for (Config.PackageEntry entry : config.packages) {
            if (uid == entry.uid) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public Config.PackageEntry find(int uid) {
        synchronized (this) {
            return findLocked(uid);
        }
    }

    private void updateLocked(int uid, int mask, int values) {
        Config.PackageEntry entry = findLocked(uid);
        if (entry == null) {
            entry = new Config.PackageEntry(uid, mask & values);
            config.packages.add(entry);
        } else {
            int newValue = (entry.flags & ~mask) | (mask & values);
            if (newValue == entry.flags) {
                return;
            }
            entry.flags = newValue;
        }
        scheduleWriteLocked();
    }

    public void update(int uid, int mask, int values) {
        synchronized (this) {
            updateLocked(uid, mask, values);
        }
    }

    private void removeLocked(int uid) {
        Config.PackageEntry entry = findLocked(uid);
        if (entry == null) {
            return;
        }
        config.packages.remove(entry);
        scheduleWriteLocked();
    }

    public void remove(int uid) {
        synchronized (this) {
            removeLocked(uid);
        }
    }
}