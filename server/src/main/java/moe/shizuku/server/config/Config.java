package moe.shizuku.server.config;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static final int LATEST_VERSION = 1;

    public static final int FLAG_ALLOWED = 1 << 1;
    public static final int FLAG_DENIED = 1 << 2;
    public static final int MASK_PERMISSION = FLAG_ALLOWED | FLAG_DENIED;

    @SerializedName("version")
    public int version = LATEST_VERSION;

    @SerializedName("packages")
    public List<PackageEntry> packages = new ArrayList<>();

    public static class PackageEntry {

        @SerializedName("uid")
        public final int uid;

        @SerializedName("flags")
        public int flags;

        public PackageEntry(int uid, int flags) {
            this.uid = uid;
            this.flags = flags;
        }

        public boolean isAllowed() {
            return (flags & FLAG_ALLOWED) != 0;
        }

        public boolean isDenied() {
            return (flags & FLAG_DENIED) != 0;
        }
    }

    public Config() {
    }

    public Config(@NonNull List<PackageEntry> packages) {
        this.version = LATEST_VERSION;
        this.packages = packages;
    }
}
