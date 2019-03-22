package moe.shizuku.manager.legacy;

import android.content.Intent;

import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.app.BaseActivity;
import moe.shizuku.manager.BuildConfig;

public abstract class AuthorizationActivity extends BaseActivity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    public ShizukuState getServerState() {
        return Single.fromCallable(ShizukuClient::getState).subscribeOn(Schedulers.io()).blockingGet();
    }

    public void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = ShizukuManagerSettings.getToken();
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
