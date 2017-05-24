package android.net.wifi;

import android.os.IBinder;

import java.util.List;

/**
 * Created by Rikka on 2017/5/15.
 */

public interface IWifiManager {

    List<WifiConfiguration> getPrivilegedConfiguredNetworks();

    abstract class Stub {

        public static IWifiManager asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}
