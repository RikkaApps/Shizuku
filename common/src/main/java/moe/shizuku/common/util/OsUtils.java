package moe.shizuku.common.util;

import android.os.SELinux;

public class OsUtils {

    private static final int UID = android.system.Os.getuid();
    private static final int PID = android.system.Os.getpid();
    private static final String SELINUX_CONTEXT;

    static {
        String context;
        try {
            context = SELinux.getContext();
        } catch (Throwable tr) {
            context = null;
        }
        SELINUX_CONTEXT = context;
    }


    public static int getUid() {
        return UID;
    }

    public static int getPid() {
        return PID;
    }

    public static String getSELinuxContext() {
        return SELINUX_CONTEXT;
    }
}

