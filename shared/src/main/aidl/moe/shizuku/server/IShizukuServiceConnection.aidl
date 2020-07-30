package moe.shizuku.server;

interface IShizukuServiceConnection {

    oneway void connected(IBinder service);

    oneway void dead();
}