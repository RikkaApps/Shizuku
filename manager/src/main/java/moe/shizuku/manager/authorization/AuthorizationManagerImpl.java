package moe.shizuku.manager.authorization;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.util.List;

public interface AuthorizationManagerImpl {

    default void init(Context context) {}

    List<PackageInfo> getPackages(int pmFlags);

    boolean granted(String packageName, int uid);

    void grant(String packageName, int uid);

    void revoke(String packageName, int uid);
}
