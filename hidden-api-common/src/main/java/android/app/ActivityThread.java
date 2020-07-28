package android.app;

public class ActivityThread {

    public static ActivityThread systemMain() {
        throw new RuntimeException();
    }

    public ContextImpl getSystemContext() {
        throw new RuntimeException();
    }
}
