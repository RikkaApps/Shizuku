package rikka.shizuku.server;

import static rikka.shizuku.server.ServerConstants.PERMISSION;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kotlin.collections.ArraysKt;
import rikka.hidden.compat.PackageManagerApis;
import rikka.hidden.compat.PermissionManagerApis;
import rikka.hidden.compat.UserManagerApis;
import rikka.shizuku.server.ktx.HandlerKt;

public class ShizukuConfigManager extends ConfigManager {

    private static final Gson GSON_IN = new GsonBuilder()
            .create();
    private static final Gson GSON_OUT = new GsonBuilder()
            .setVersion(ShizukuConfig.LATEST_VERSION)
            .create();

    private static final long WRITE_DELAY = 10 * 1000;

    private static final AtomicFile ATOMIC_FILE;

    static {
        File FILE = null;
        String dir = "/data/local/tmp";
        File directory = new File(dir);
        String[] files = directory.list();

        if (files != null) {
            for (String file : files) {
                if (file.matches("^shizuku-[A-Za-z0-9]{6}$")) {
                    FILE = new File(dir + "/" + file + "/shizuku.json");
                }
            }
            LOGGER.d("Found: " + FILE.getAbsolutePath());
        }

        if (FILE == null) {
            LOGGER.i("no existing config file");
            System.exit(255);
        }

        ATOMIC_FILE = new AtomicFile(FILE);
    }

    public static ShizukuConfig load() {
        FileInputStream stream;
        try {
            stream = ATOMIC_FILE.openRead();
        } catch (FileNotFoundException e) {
            LOGGER.i("no existing config file " + ATOMIC_FILE.getBaseFile() + "; starting empty");
            return new ShizukuConfig();
        }

        ShizukuConfig config = null;
        try {
            config = GSON_IN.fromJson(new InputStreamReader(stream), ShizukuConfig.class);
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

    public static void write(ShizukuConfig config) {
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

    private final Runnable mWriteRunner = new Runnable() {

        @Override
        public void run() {
            write(config);
        }
    };

    private final ShizukuConfig config;

    public ShizukuConfigManager() {
        this.config = load();

        boolean changed = false;

        if (config.packages == null) {
            config.packages = new ArrayList<>();
            changed = true;
        }

        if (config.version < 2) {
            for (ShizukuConfig.PackageEntry entry : new ArrayList<>(config.packages)) {
                entry.packages = PackageManagerApis.getPackagesForUidNoThrow(entry.uid);
            }
            changed = true;
        }

        for (ShizukuConfig.PackageEntry entry : new ArrayList<>(config.packages)) {
            if (entry.packages == null) {
                entry.packages = new ArrayList<>();
            }

            List<String> packages = PackageManagerApis.getPackagesForUidNoThrow(entry.uid);
            if (packages.isEmpty()) {
                LOGGER.i("remove config for uid %d since it has gone", entry.uid);
                config.packages.remove(entry);
                changed = true;
                continue;
            }

            boolean packagesChanged = true;

            for (String packageName : entry.packages) {
                if (packages.contains(packageName)) {
                    packagesChanged = false;
                    break;
                }
            }

            final int rawSize = entry.packages.size();
            Set<String> s = new LinkedHashSet<>(entry.packages);
            entry.packages.clear();
            entry.packages.addAll(s);
            final int shrunkSize = entry.packages.size();
            if (shrunkSize < rawSize) {
                LOGGER.w("entry.packages has duplicate! Shrunk. (%d -> %d)", rawSize, shrunkSize);
            }

            if (packagesChanged) {
                LOGGER.i("remove config for uid %d since the packages for it changed", entry.uid);
                config.packages.remove(entry);
                changed = true;
            }
        }

        for (int userId : UserManagerApis.getUserIdsNoThrow()) {
            for (PackageInfo pi : PackageManagerApis.getInstalledPackagesNoThrow(PackageManager.GET_PERMISSIONS, userId)) {
                if (pi == null
                        || pi.applicationInfo == null
                        || pi.requestedPermissions == null
                        || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                int uid = pi.applicationInfo.uid;
                boolean allowed;
                try {
                    allowed = PermissionManagerApis.checkPermission(PERMISSION, uid) == PackageManager.PERMISSION_GRANTED;
                } catch (Throwable e) {
                    LOGGER.w("checkPermission");
                    continue;
                }

                List<String> packages = new ArrayList<>();
                packages.add(pi.packageName);

                updateLocked(uid, packages, ConfigManager.MASK_PERMISSION, allowed ? ConfigManager.FLAG_ALLOWED : 0);
                changed = true;
            }
        }

        if (changed) {
            scheduleWriteLocked();
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

    private ShizukuConfig.PackageEntry findLocked(int uid) {
        for (ShizukuConfig.PackageEntry entry : config.packages) {
            if (uid == entry.uid) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public ShizukuConfig.PackageEntry find(int uid) {
        synchronized (this) {
            return findLocked(uid);
        }
    }

    private void updateLocked(int uid, List<String> packages, int mask, int values) {
        ShizukuConfig.PackageEntry entry = findLocked(uid);
        if (entry == null) {
            entry = new ShizukuConfig.PackageEntry(uid, mask & values);
            config.packages.add(entry);
        } else {
            int newValue = (entry.flags & ~mask) | (mask & values);
            if (newValue == entry.flags) {
                return;
            }
            entry.flags = newValue;
        }
        if (packages != null) {
            for (String packageName : packages) {
                if (entry.packages.contains(packageName)) {
                    continue;
                }
                entry.packages.add(packageName);
            }
        }
        scheduleWriteLocked();
    }

    public void update(int uid, List<String> packages, int mask, int values) {
        synchronized (this) {
            updateLocked(uid, packages, mask, values);
        }
    }

    private void removeLocked(int uid) {
        ShizukuConfig.PackageEntry entry = findLocked(uid);
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
