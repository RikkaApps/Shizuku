-repackageclasses moe.shizuku.server

-keepnames class moe.shizuku.api.BinderContainer

-keep class moe.shizuku.server.Starter {
    public static void main(java.lang.String[]);
}