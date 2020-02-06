package moe.shizuku.server;

import moe.shizuku.server.IRemoteProcess;

interface IShizukuService {

    int getVersion() = 2;

    int getUid() = 3;

    int checkPermission(String permission) = 4;

    String getToken() = 5;

    boolean setUidToken(in String token) = 6;

    IRemoteProcess newProcess(in String[] cmd, in String[] env, in String dir) = 7;

    String getSELinuxContext() = 8;

    // manager only
    void setUidPermissionPre23(int uid, boolean granted) = 1000;
}
