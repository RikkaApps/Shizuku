package moe.shizuku.server.content;

import android.content.IIntentReceiver;
import android.content.Intent;
import android.os.Bundle;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Rikka on 2017/5/6.
 */

public class IntentReceiver extends IIntentReceiver.Stub {

    private final CountDownLatch mLatch;

    public IntentReceiver(CountDownLatch latch) {
        mLatch = latch;
    }

    @Override
    public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
                               boolean ordered, boolean sticky, int sendingUser) {
        //String line = "Broadcast completed: result=" + resultCode + " ,intent=" + intent;
        //if (data != null) line = line + ", data=\"" + data + "\"";
        //if (extras != null) line = line + ", extras: " + extras;
        //ServerLog.i(line);

        mLatch.countDown();
    }
}
