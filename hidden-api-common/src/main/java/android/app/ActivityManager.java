package android.app;

import androidx.annotation.RequiresApi;

public class ActivityManager {

    public static int UID_OBSERVER_ACTIVE;

    public static int PROCESS_STATE_UNKNOWN;

    public static class RunningAppProcessInfo {

        public static int procStateToImportance(int procState) {
            throw new RuntimeException("STUB");
        }
    }
}
