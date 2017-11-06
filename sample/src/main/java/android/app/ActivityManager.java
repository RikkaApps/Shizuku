package android.app;

import android.content.ComponentName;
import android.graphics.Bitmap;

/**
 * Created by rikka on 2017/11/6.
 */

public class ActivityManager {

    public static class TaskDescription {

    }

    public static class TaskSnapshot {

    }

    public static class RunningTaskInfo {

        public int id;
        public int stackId;
        public ComponentName baseActivity;
        public ComponentName topActivity;
        public Bitmap thumbnail;
        public CharSequence description;
        public int numActivities;
        public int numRunning;
        public long lastActiveTime;
        public boolean supportsSplitScreenMultiWindow;
        public int resizeMode;
    }
}
