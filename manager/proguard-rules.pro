-repackageclasses rikka.shizuku

# Kotlin

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void checkExpressionValueIsNotNull(...);
	public static void checkNotNullExpressionValue(...);
	public static void checkReturnedValueIsNotNull(...);
	public static void checkFieldIsNotNull(...);
	public static void checkParameterIsNotNull(...);
}

-keepnames class moe.shizuku.api.BinderContainer

# Missing class android.app.IProcessObserver$Stub
# Missing class android.app.IUidObserver$Stub
-keepclassmembers class hidden.ProcessObserverAdapter {
    <methods>;
}

-keepclassmembers class hidden.UidObserverAdapter {
    <methods>;
}

# Entrance of Shizuku service
-keep class moe.shizuku.server.ShizukuService {
    public static void main(java.lang.String[]);
}

# Entrance of user service starter
-keep class moe.shizuku.starter.ServiceStarter {
    public static void main(java.lang.String[]);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

-assumenosideeffects class moe.shizuku.manager.utils.Logger {
    public *** d(...);
}

-assumenosideeffects class moe.shizuku.server.utils.Logger {
    public *** d(...);
}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
