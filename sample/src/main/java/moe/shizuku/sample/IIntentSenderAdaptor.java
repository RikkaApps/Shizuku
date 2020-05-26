package moe.shizuku.sample;

import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public abstract class IIntentSenderAdaptor extends IIntentSender.Stub {

    public abstract void send(Intent intent);

    @Override
    public int send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        send(intent);
        return 0;
    }

    @Override
    public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        send(intent);
    }
}
