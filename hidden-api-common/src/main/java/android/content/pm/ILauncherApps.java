package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.RequiresApi;

public interface ILauncherApps extends IInterface {

    void addOnAppsChangedListener(IOnAppsChangedListener listener);

    @RequiresApi(24)
    void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener);

    abstract class Stub extends Binder implements ILauncherApps {

        public static ILauncherApps asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
