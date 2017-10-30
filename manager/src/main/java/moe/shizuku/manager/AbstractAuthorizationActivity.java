package moe.shizuku.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.fontprovider.FontProviderClient;

public abstract class AbstractAuthorizationActivity extends Activity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    private static boolean sFontInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontInitialized) {
            FontProviderClient client = FontProviderClient.create(this);
            if (client != null) {
                client.replace("Noto Sans CJK",
                        "sans-serif", "sans-serif-medium");
            }

            sFontInitialized = true;
        }

        super.onCreate(savedInstanceState);
    }

    public ShizukuState getServerState() {
        return Single.fromCallable(new Callable<ShizukuState>() {
            @Override
            public ShizukuState call() throws Exception {
                return ShizukuClient.getState();
            }
        }).subscribeOn(Schedulers.io()).blockingGet();
    }

    public void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = ShizukuManagerSettings.getToken(this);
            Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

            setResult(ShizukuClient.AUTH_RESULT_OK, intent);
        } else {
            setResult(ShizukuClient.AUTH_RESULT_USER_DENIED);
        }
    }
}
