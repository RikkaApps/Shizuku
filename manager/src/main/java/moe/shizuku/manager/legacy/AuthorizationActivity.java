package moe.shizuku.manager.legacy;

import android.content.Intent;

import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.BuildConfig;
import moe.shizuku.manager.app.BaseActivity;
import moe.shizuku.server.IShizukuService;

public abstract class AuthorizationActivity extends BaseActivity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    public ShizukuLegacy.ShizukuState getLegacyServerState() {
        return Single.fromCallable(ShizukuLegacy.ShizukuClient::getState).subscribeOn(Schedulers.io()).blockingGet();
    }

    public boolean isV3() {
        return getIntent().getBooleanExtra(ShizukuApiConstants.EXTRA_PRE_23_IS_V3, false);
    }

    public void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = null;
            if (isV3()) {
                IShizukuService service = IShizukuService.Stub.asInterface(ShizukuService.getBinder());
                if (service != null) {
                    try {
                        token = UUID.fromString(service.getToken());
                    } catch (Throwable ignored) {
                    }
                }
            } else {
                token = ShizukuLegacy.getToken();
            }

            Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName);

            if (token == null) {
                setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR, intent);
                return;
            }

            intent.putExtra(ShizukuLegacy.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                    .putExtra(ShizukuLegacy.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_OK, intent);
        } else {
            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_USER_DENIED);
        }
    }
}
