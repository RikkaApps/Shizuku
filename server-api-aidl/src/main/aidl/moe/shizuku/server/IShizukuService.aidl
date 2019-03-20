package moe.shizuku.server;

interface IShizukuService {

    int getVersion() = 1;
    int getUid() = 2;
    int checkPermission(String permission) = 3;
}
