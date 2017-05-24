package android.content;

import android.os.Bundle;
import android.os.RemoteException;

/**
 * Created by Rikka on 2017/5/6.
 */

public interface IIntentReceiver {

    void performReceive(Intent intent, int resultCode, String data,
                        Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException;

    abstract class Stub implements IIntentReceiver {

    }
}
