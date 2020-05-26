package moe.shizuku.sample;

import android.content.IIntentSender;
import android.content.IntentSender;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;

public class IntentSenderUtils {

    public static IntentSender newInstance(IIntentSender binder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return IntentSender.class.getConstructor(IIntentSender.class).newInstance(binder);
    }
}
