package android.content;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.RequiresApi;

public interface IIntentSender extends IInterface {

    int send(int code, Intent intent, String resolvedType,
             IIntentReceiver finishedReceiver, String requiredPermission, Bundle options);

    @RequiresApi(26)
    void send(int code, Intent intent, String resolvedType, IBinder whitelistToken,
              IIntentReceiver finishedReceiver, String requiredPermission, Bundle options);

    abstract class Stub extends Binder implements IIntentSender {

        public Stub() {
            throw new UnsupportedOperationException();
        }

        @Override
        public android.os.IBinder asBinder() {
            throw new UnsupportedOperationException();
        }

        public static IIntentSender asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}
