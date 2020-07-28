package android.content;

import android.content.pm.PackageManager;
import android.os.UserHandle;

public class Context {

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
            throws PackageManager.NameNotFoundException {
        throw new RuntimeException();
    }
}
