package moe.shizuku.manager.utils;

import android.system.Os;

public class UserHandleCompat {

    private static final int MY_USER_ID = getUserId(Os.getuid());

    public static final int PER_USER_RANGE = 100000;

    public static int getUserId(int uid) {
        return uid / PER_USER_RANGE;
    }

    public static int getAppId(int uid) {
        return uid % PER_USER_RANGE;
    }

    public static int myUserId() {
        return MY_USER_ID;
    }
}