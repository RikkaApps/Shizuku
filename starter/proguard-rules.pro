-repackageclasses moe.shizuku.server

# Kotlin

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void checkExpressionValueIsNotNull(...);
	public static void checkNotNullExpressionValue(...);
	public static void checkReturnedValueIsNotNull(...);
	public static void checkFieldIsNotNull(...);
	public static void checkParameterIsNotNull(...);
}

-keepnames class moe.shizuku.api.BinderContainer

-keep class moe.shizuku.starter.ServiceStarter {
    public static void main(java.lang.String[]);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception