package android.content;

import android.os.Binder;
import android.os.Bundle;

public interface IIntentReceiver {

    void performReceive(Intent intent, int resultCode, String data, Bundle extras,
                        boolean ordered, boolean sticky, int sendingUser)
            throws android.os.RemoteException;

    abstract class Stub extends Binder implements IIntentReceiver {

    }
}
