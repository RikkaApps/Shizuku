-repackageclasses moe.shizuku.server

-keepnames class moe.shizuku.api.BinderContainer

-keep class moe.shizuku.server.Starter {
    public static void main(java.lang.String[]);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception