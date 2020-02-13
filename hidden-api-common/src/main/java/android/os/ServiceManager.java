package android.os;

import android.annotation.Nullable;

public class ServiceManager {

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    @Nullable
    public static IBinder getService(String name) {
        throw new RuntimeException("STUB");
    }

    /**
     * Place a new @a service called @a name into the service
     * manager.
     *
     * @param name the name of the new service
     * @param service the service object
     */
    public static void addService(String name, IBinder service) {
        throw new RuntimeException("STUB");
    }
}
