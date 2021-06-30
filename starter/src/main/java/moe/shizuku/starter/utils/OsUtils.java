package moe.shizuku.starter.utils;

public class OsUtils {

    private static final int UID = android.system.Os.getuid();

    public static int getUid() {
        return UID;
    }

}

