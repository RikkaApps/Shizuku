package android.app;

import java.util.List;

public class AppOpsManager {

    public static int permissionToOpCode(String permission) {
        throw new RuntimeException("STUB");
    }

    public static class PackageOps {

        public String getPackageName() {
            throw new RuntimeException("STUB");
        }

        public int getUid() {
            throw new RuntimeException("STUB");
        }

        public List<OpEntry> getOps() {
            throw new RuntimeException("STUB");
        }
    }
    
    public static class OpEntry {

        public int getOp() {
            throw new RuntimeException("STUB");
        }

        public int getMode() {
            throw new RuntimeException("STUB");
        }

        public long getTime() {
            throw new RuntimeException("STUB");
        }

        public long getRejectTime() {
            throw new RuntimeException("STUB");
        }

        public boolean isRunning() {
            throw new RuntimeException("STUB");
        }

        public int getDuration() {
            throw new RuntimeException("STUB");
        }

        public int getProxyUid() {
            throw new RuntimeException("STUB");
        }

        public String getProxyPackageName() {
            throw new RuntimeException("STUB");
        }
    }
}
