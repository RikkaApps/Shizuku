package moe.shizuku.manager.legacy.authorization;

import android.content.Context;

import java.util.List;

/**
 * Created by rikka on 2017/10/23.
 */

public interface AuthorizationManagerImpl {

    List<String> getPackages(Context context);

    boolean granted(Context context, String packageName);

    void grant(Context context, String packageName);

    void revoke(Context context, String packageName);
}
