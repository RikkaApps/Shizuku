package moe.shizuku.manager.authorization;

import android.content.Context;

import java.util.List;

public interface AuthorizationManagerImpl {

    default void init(Context context) {}

    List<String> getPackages();

    boolean granted(String packageName);

    void grant(String packageName);

    void revoke(String packageName);
}
