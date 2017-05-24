package android.app;

import java.util.List;

/**
 * Created by Rikka on 2017/5/9.
 */

public class AppOpsManager {

    public static int OP_NONE = -1;

    public static class PackageOps {

        public String getPackageName() {
            throw new UnsupportedOperationException();
        }

        public int getUid() {
            throw new UnsupportedOperationException();
        }

        public List<OpEntry> getOps() {
            throw new UnsupportedOperationException();
        }
    }

    public static class OpEntry {

        public int getOp() {
            throw new UnsupportedOperationException();
        }

        public int getMode() {
            throw new UnsupportedOperationException();
        }

        public long getTime() {
            throw new UnsupportedOperationException();
        }

        public long getRejectTime() {
            throw new UnsupportedOperationException();
        }

        public boolean isRunning() {
            throw new UnsupportedOperationException();
        }

        public int getDuration() {
            throw new UnsupportedOperationException();
        }

        public int getProxyUid() {
            throw new UnsupportedOperationException();
        }

        public String getProxyPackageName() {
            throw new UnsupportedOperationException();
        }
    }
}
