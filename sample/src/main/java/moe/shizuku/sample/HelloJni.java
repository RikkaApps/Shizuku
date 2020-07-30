package moe.shizuku.sample;

public class HelloJni {

    static {
        System.loadLibrary("hello-jni");
    }

    public static native String stringFromJNI();
}
